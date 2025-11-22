package com.example.batch.rabbitmq;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

@ApplicationScoped
public class BatchProducer {

    @Channel("batch-queue-out")
    Emitter<String> emitter;

    public void send(String message, String routingKey) {
        Metadata metadata = Metadata.of(
                new OutgoingRabbitMQMetadata.Builder()
                        .withRoutingKey(routingKey)
                        .build());
        emitter.send(Message.of(message, metadata));
    }
}
