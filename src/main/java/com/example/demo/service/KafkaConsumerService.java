package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class KafkaConsumerService {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final MessageRepository messageRepository;
    private final MeterRegistry meterRegistry;

    public KafkaConsumerService(MessageRepository messageRepository, MeterRegistry meterRegistry) {
        this.messageRepository = messageRepository;
        this.meterRegistry = meterRegistry;
    }
    
    @KafkaListener(topics = "demo-topic")
    public void listen(String message) {
        Counter.builder("kafka.messages.received")
              .tag("topic", "demo-topic")
              .register(meterRegistry)
              .increment();
              
        Message msg = new Message();
        msg.setContent(message);
        messageRepository.save(msg);
        
        log.info("Received message: {}", message);
    }
}
