package com.example.batch.config;

import com.example.batch.rabbitmq.BatchProducer;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RabbitMQWarmup {

    @Inject
    BatchProducer producer;

    @ConfigProperty(name = "rabbitmq-queue")
    String queueName;

    void onStart(@Observes StartupEvent ev) {
        // Send a dummy message to force connection and queue declaration
        // This ensures the queue exists even before the first user command
        System.out.println("Warming up RabbitMQ connection for queue: " + queueName);
        try {
            // We send a message that will be consumed (or just stay there)
            // Since we don't have a consumer in this app, it will just sit in the queue.
            // This confirms the queue is created.
            producer.send("RabbitMQ Warmup Message - " + java.time.LocalDateTime.now(), "warmup");
        } catch (Exception e) {
            System.err.println("Failed to warmup RabbitMQ: " + e.getMessage());
        }
    }
}
