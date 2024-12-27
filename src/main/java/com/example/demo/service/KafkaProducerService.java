package com.example.demo.service;

import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;

@Service
public class KafkaProducerService {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageRepository messageRepository;
    private final MeterRegistry meterRegistry;
    

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, MessageRepository messageRepository, MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.messageRepository = messageRepository;
        this.meterRegistry = meterRegistry;
    }

    public void sendMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            log.warn("Attempted to send an empty or null message. Skipping...");
            return;  // 可以抛出异常，或者直接返回
        }

        log.info("Sending message: {}", message); 

        Counter.builder("kafka.messages.sent")
              .tag("topic", "demo-topic")
              .register(meterRegistry)
              .increment();
              
        Message msg = new Message();
        msg.setContent(message);
        messageRepository.save(msg);
        
        kafkaTemplate.send("demo-topic", message)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message [{}] sent successfully to topic [{}] with offset [{}]",
                            message, "demo-topic", result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send message [{}] to topic [{}]", message, "demo-topic", ex);
                }
            });
    }
}
