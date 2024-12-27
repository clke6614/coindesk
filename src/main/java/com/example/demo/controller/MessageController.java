package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;
import com.example.demo.service.KafkaProducerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    private final KafkaProducerService producerService;
    private final MessageRepository messageRepository;

    public MessageController(KafkaProducerService producerService, MessageRepository messageRepository) {
        this.producerService = producerService;
        this.messageRepository = messageRepository;
    }
    
    @PostMapping 
    public ResponseEntity<?> sendMessage(@RequestBody Message message) {
        try {
            log.info("Received message: {}", message.getContent());
            // 先保存消息
            Message savedMessage = messageRepository.save(message);
            // 發送到 Kafka
            producerService.sendMessage(savedMessage.getContent());
            return ResponseEntity.ok("Message sent successfully");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid message received: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to process message");
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Message>> getMessages() {
        return ResponseEntity.ok(messageRepository.findAll());
    }
}
