package com.example.batch.scheduler;

import com.example.batch.rabbitmq.BatchProducer;
import io.quarkus.scheduler.Scheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.quarkus.runtime.annotations.RegisterForReflection;

@ApplicationScoped
@RegisterForReflection(targets = { BatchScheduler.BatchJob.class })
public class BatchScheduler {

    @Inject
    Scheduler scheduler;

    @Inject
    BatchProducer producer;

    @Inject
    com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private final Map<String, BatchJob> jobs = new ConcurrentHashMap<>();
    private final java.nio.file.Path storagePath = java.nio.file.Paths.get("data", "batch-jobs.json");

    @jakarta.annotation.PostConstruct
    void init() {
        // Ensure data directory exists
        try {
            java.nio.file.Files.createDirectories(storagePath.getParent());
        } catch (Exception e) {
            System.err.println("데이터 디렉토리 생성 실패: " + e.getMessage());
        }
        loadJobs();
    }

    public void schedule(String name, String cron, String routingKey, String message) {
        if (jobs.containsKey(name)) {
            throw new IllegalArgumentException("이미 존재하는 배치 이름입니다: " + name);
        }

        try {
            doSchedule(name, cron, routingKey, message);
            jobs.put(name, new BatchJob(name, cron, routingKey, message));
            saveJobs();
        } catch (Exception e) {
            throw new IllegalArgumentException("스케줄링 실패: " + e.getMessage());
        }
    }

    private void doSchedule(String name, String cron, String routingKey, String message) {
        scheduler.newJob(name)
                .setCron(cron)
                .setTask(executionContext -> producer.send(message, routingKey))
                .schedule();
    }

    public void remove(String name) {
        if (!jobs.containsKey(name)) {
            throw new IllegalArgumentException("존재하지 않는 배치입니다: " + name);
        }
        scheduler.unscheduleJob(name);
        jobs.remove(name);
        saveJobs();
    }

    public void execute(String name) {
        BatchJob job = jobs.get(name);
        if (job == null) {
            throw new IllegalArgumentException("존재하지 않는 배치입니다: " + name);
        }
        producer.send(job.message(), job.routingKey());
    }

    public String list(int limit) {
        if (jobs.isEmpty()) {
            return "등록된 배치가 없습니다.";
        }
        return jobs.values().stream()
                .limit(limit)
                .map(job -> String.format("- %s: [%s] %s -> %s", job.name(), job.routingKey(), job.cron(),
                        job.message()))
                .collect(Collectors.joining("\n"));
    }

    private void saveJobs() {
        try {
            objectMapper.writeValue(storagePath.toFile(), jobs.values());
        } catch (Exception e) {
            System.err.println("배치 저장 실패: " + e.getMessage());
        }
    }

    private void loadJobs() {
        if (!storagePath.toFile().exists()) {
            return;
        }
        try {
            java.util.List<BatchJob> loadedJobs = objectMapper.readValue(
                    storagePath.toFile(),
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<BatchJob>>() {
                    });
            for (BatchJob job : loadedJobs) {
                try {
                    doSchedule(job.name(), job.cron(), job.routingKey(), job.message());
                    jobs.put(job.name(), job);
                } catch (Exception e) {
                    System.err.println("배치 복구 실패 (" + job.name() + "): " + e.getMessage());
                }
            }
            System.out.println(loadedJobs.size() + "개의 배치가 복구되었습니다.");
        } catch (Exception e) {
            System.err.println("배치 파일 로드 실패: " + e.getMessage());
        }
    }

    public record BatchJob(String name, String cron, String routingKey, String message) {
    }
}
