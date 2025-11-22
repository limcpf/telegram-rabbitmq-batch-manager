package com.example.batch.rabbitmq;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class BatchProducer {

    @Channel("batch-queue-out")
    Emitter<String> emitter;

    public void send(String message) {
        emitter.send(message);
    }
}
