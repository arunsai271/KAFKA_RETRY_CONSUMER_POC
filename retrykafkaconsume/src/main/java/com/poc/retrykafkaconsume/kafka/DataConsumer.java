package com.poc.retrykafkaconsume.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataConsumer {

	@KafkaListener(topics = "#{'${spring.kafka.consumer.topic}'.split(',')}", groupId = "test-group-1")
	public void listenTestEvents(final ConsumerRecord<Integer, String> message) {

		log.info("Received TestEvent with headers: {}, value: {}", message.headers(), message.value());

		if (true) {
			throw new RuntimeException();
		}

	}
}
