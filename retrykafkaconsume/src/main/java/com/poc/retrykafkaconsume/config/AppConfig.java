package com.poc.retrykafkaconsume.config;

import javax.management.InvalidApplicationException;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.util.backoff.FixedBackOff;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AppConfig {

	@Bean
	public ConsumerFactory<Integer, String> consumerFactory(KafkaProperties kafkaProperties) {
		return new DefaultKafkaConsumerFactory<Integer, String>(kafkaProperties.buildConsumerProperties());
	}

	@Bean
	public DeadLetterPublishingRecoverer deadLetterPublishRecoverer(final KafkaTemplate<String, String> template,
			@Value("${spring.kafka.recovery.dlt-topic}") String dltTopic) {

		return new DeadLetterPublishingRecoverer(template, (r, e) -> {
			log.info("############### Pushing Data into DLT Topic ##################");
			return new TopicPartition(dltTopic, r.partition());
		});

	}

	@Bean
	public DefaultErrorHandler defaultErrorHandler(final DeadLetterPublishingRecoverer deadLetterPublishRecoverer,
			final @Value("#{new Long('${spring.kafka.retry.backoff-interval}')}") Long interval,
			final @Value("#{new Long('${spring.kafka.retry.backoff-maxAttempts}')}") Long maxAttempts,
			final @Value("${spring.kafka.retry.errorhandler.resetStateOnRecoveryFailure}") boolean isResetStateEnabled
	/*
	 * final @Value("${spring.kafka.retry.errorhandler.skipexceptions}")
	 * List<Class<? extends Exception>> skipExceptions
	 */) {

		final DefaultErrorHandler defaulErrorHandler = new DefaultErrorHandler(deadLetterPublishRecoverer,
				new FixedBackOff(interval, maxAttempts));
		defaulErrorHandler.setResetStateOnRecoveryFailure(isResetStateEnabled);
		defaulErrorHandler.addNotRetryableExceptions(MethodArgumentNotValidException.class,
				InvalidApplicationException.class);
		// skipExceptions.forEach(defaulErrorHandler::addNotRetryableExceptions);
		return defaulErrorHandler;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory(
			final ConsumerFactory<Integer, String> consumerFactory, final DefaultErrorHandler defaultErrorHandler) {

		final ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(defaultErrorHandler);
		return factory;
	}

	@Bean
	public NewTopic topic1(final @Value("${spring.kafka.consumer.topic}") String topicName) {
		return TopicBuilder.name(topicName).partitions(10).replicas(1).build();
	}

	@Bean
	public NewTopic dlt(final @Value("${spring.kafka.recovery.dlt-topic}") String topicName) {
		return TopicBuilder.name(topicName).partitions(10).replicas(1).build();
	}

}
