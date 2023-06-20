package com.bridle.configuration;

import com.bridle.properties.HttpProducerConfiguration;
import com.bridle.properties.ValidatedKafkaConsumerConfiguration;
import com.bridle.properties.ValidatedKafkaProducerConfiguration;
import com.bridle.routes.KafkaHttpKafkaRoute;
import com.bridle.utils.ComponentCustomizerImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.spi.ComponentCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static com.bridle.configuration.ComponentNameConstants.KAFKA_IN_COMPONENT_NAME;
import static com.bridle.configuration.ComponentNameConstants.KAFKA_OUT_COMPONENT_NAME;
import static com.bridle.configuration.ComponentNameConstants.REST_CALL_COMPONENT_NAME;
import static com.bridle.configuration.KafkaHttpKafkaConfiguration.GATEWAY_TYPE_KAFKA_HTTP_KAFKA;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.http;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.kafka;

@Configuration
@ConditionalOnProperty(name = "gateway.type", havingValue = GATEWAY_TYPE_KAFKA_HTTP_KAFKA)
public class KafkaHttpKafkaConfiguration {

    public static final String GATEWAY_TYPE_KAFKA_HTTP_KAFKA = "kafka-http-kafka";

    @ConfigurationProperties(prefix = KAFKA_IN_COMPONENT_NAME)
    @Bean
    public ValidatedKafkaConsumerConfiguration kafkaInConfiguration() {
        return new ValidatedKafkaConsumerConfiguration();
    }

    @Bean(name = KAFKA_IN_COMPONENT_NAME)
    public KafkaComponent kafkaInComponent() {
        return new KafkaComponent();
    }

    @Lazy
    @Bean
    public ComponentCustomizer configureKafkaInComponent(CamelContext context,
                                                         ValidatedKafkaConsumerConfiguration componentConfiguration) {
        return new ComponentCustomizerImpl(context, componentConfiguration, KAFKA_IN_COMPONENT_NAME);
    }

    @ConfigurationProperties(prefix = REST_CALL_COMPONENT_NAME)
    @Bean
    public HttpProducerConfiguration restCallConfiguration() {
        return new HttpProducerConfiguration();
    }

    @Bean(name = REST_CALL_COMPONENT_NAME)
    public HttpComponent restCallComponent() {
        return new HttpComponent();
    }

    @Lazy
    @Bean
    public ComponentCustomizer configureHttpComponent(CamelContext context,
                                                      HttpProducerConfiguration componentConfiguration) {
        return new ComponentCustomizerImpl(context, componentConfiguration, REST_CALL_COMPONENT_NAME);
    }

    @ConfigurationProperties(prefix = KAFKA_OUT_COMPONENT_NAME)
    @Bean
    public ValidatedKafkaProducerConfiguration kafkaOutConfiguration() {
        return new ValidatedKafkaProducerConfiguration();
    }

    @Bean(name = KAFKA_OUT_COMPONENT_NAME)
    public KafkaComponent kafkaOutComponent() {
        return new KafkaComponent();
    }

    @Lazy
    @Bean
    public ComponentCustomizer configureKafkaOutComponent(CamelContext context,
                                                          ValidatedKafkaProducerConfiguration componentConfiguration) {
        return new ComponentCustomizerImpl(context, componentConfiguration, KAFKA_OUT_COMPONENT_NAME);
    }

    @Bean
    public RouteBuilder kafkaHttpKafkaRoute(ValidatedKafkaConsumerConfiguration kafkaInConfiguration,
                                            HttpProducerConfiguration restConfiguration,
                                            ValidatedKafkaProducerConfiguration kafkaOutConfiguration) {

        EndpointConsumerBuilder kafkaIn = kafka(KAFKA_IN_COMPONENT_NAME, kafkaInConfiguration.getTopic());
        kafkaInConfiguration.getEndpointProperties().
                ifPresent(additional -> additional.forEach(kafkaIn::doSetProperty));

        EndpointProducerBuilder http = http(REST_CALL_COMPONENT_NAME, restConfiguration.createHttpUrl());
        restConfiguration.getEndpointProperties().ifPresent(additional -> additional.forEach(http::doSetProperty));

        EndpointProducerBuilder kafkaOut = kafka(ComponentNameConstants.KAFKA_OUT_COMPONENT_NAME,
                kafkaOutConfiguration.getTopic());
        kafkaOutConfiguration.getEndpointProperties()
                .ifPresent(additional -> additional.forEach(kafkaOut::doSetProperty));

        return new KafkaHttpKafkaRoute(kafkaIn, http, kafkaOut);
    }
}
