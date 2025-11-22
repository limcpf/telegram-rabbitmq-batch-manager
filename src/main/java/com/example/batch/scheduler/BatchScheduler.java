package com.example.batch.scheduler;

import com.example.batch.rabbitmq.BatchProducer;
import io.quarkus.scheduler.Scheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class BatchScheduler {

    @Inject
    Scheduler scheduler;

    @Inject
    BatchProducer producer;

    private final Map<String, BatchJob> jobs = new ConcurrentHashMap<>();

    public void schedule(String name, String cron, String message) {
        if (jobs.containsKey(name)) {
            throw new IllegalArgumentException("이미 존재하는 배치 이름입니다: " + name);
        }

        try {
            scheduler.newJob(name)
                    .setCron(cron)
                    .setTask(executionContext -> producer.send(message))
                    .schedule();
            jobs.put(name, new BatchJob(name, cron, message));
        } catch (Exception e) {
            throw new IllegalArgumentException("스케줄링 실패 (크론 표현식을 확인하세요): " + e.getMessage());
        }
    }

    public void remove(String name) {
        if (!jobs.containsKey(name)) {
            throw new IllegalArgumentException("존재하지 않는 배치입니다: " + name);
        }
        scheduler.unscheduleJob(name);
        jobs.remove(name);
    }

    public void execute(String name) {
        BatchJob job = jobs.get(name);
        if (job == null) {
            throw new IllegalArgumentException("존재하지 않는 배치입니다: " + name);
        }
        producer.send(job.message());
    }

    public String list(int limit) {
        if (jobs.isEmpty()) {
            return "등록된 배치가 없습니다.";
        }
        return jobs.values().stream()
                .limit(limit)
                .map(job -> String.format("- %s: %s -> %s", job.name(), job.cron(), job.message()))
                .collect(Collectors.joining("\n"));
    }

    public record BatchJob(String name, String cron, String message) {
    }
}
