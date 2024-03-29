package io.github.comrada.crypto.wtc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.comrada.crypto.wtc.repository.WhaleAlertRepository;
import io.github.comrada.crypto.wtc.scheduling.ExecutionProperties;
import io.github.comrada.crypto.wtc.scheduling.TaskExecutor;
import io.github.comrada.crypto.wtc.scheduling.amqp.RabbitMqListener;
import io.github.comrada.crypto.wtc.model.WhaleAlert;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@EnableRabbit
public class RabbitMqConfig {

  @Bean
  public Jackson2JsonMessageConverter jsonMessageConverter() {
    ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  Queue whalesQueue() {
    return QueueBuilder
        .nonDurable()
        .autoDelete()
        .build();
  }

  @Bean
  TopicExchange exchange(ExecutionProperties properties) {
    return new TopicExchange(properties.getAmqp().getExchange());
  }

  @Bean
  Binding newAlertsBinding(Queue queue, TopicExchange exchange, ExecutionProperties properties) {
    return BindingBuilder
        .bind(queue)
        .to(exchange)
        .with(properties.getAmqp().getRoutingKey());
  }

  @Bean
  RabbitMqListener rabbitMqListener(WhaleAlertRepository alertRepository, TaskExecutor<WhaleAlert> taskExecutor) {
    return new RabbitMqListener(taskExecutor, alertRepository);
  }
}
