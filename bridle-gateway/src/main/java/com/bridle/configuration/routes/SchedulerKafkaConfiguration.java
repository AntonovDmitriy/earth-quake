package com.bridle.configuration.routes;

import com.bridle.configuration.common.consumer.SchedulerConfiguration;
import com.bridle.configuration.common.errorhandling.ErrorHandlerConfiguration;
import com.bridle.configuration.common.processing.AfterConsumerProcessingConfiguration;
import com.bridle.configuration.common.processing.AfterProducerProcessingConfiguration;
import com.bridle.configuration.common.producer.KafkaOutConfiguration;
import com.bridle.routes.ConsumerToProducerRoute;
import com.bridle.routes.model.ConsumerToProducerRouteParams;
import com.bridle.utils.ProcessingParams;
import org.apache.camel.ErrorHandlerFactory;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.bridle.configuration.routes.SchedulerKafkaConfiguration.GATEWAY_TYPE_SCHEDULER_KAFKA;

@Configuration
@Import({SchedulerConfiguration.class, KafkaOutConfiguration.class, ErrorHandlerConfiguration.class,
        AfterConsumerProcessingConfiguration.class, AfterProducerProcessingConfiguration.class})
@ConditionalOnProperty(name = "gateway.type",
        havingValue = GATEWAY_TYPE_SCHEDULER_KAFKA)
public class SchedulerKafkaConfiguration {

    public static final String GATEWAY_TYPE_SCHEDULER_KAFKA = "scheduler-kafka";

    @Bean
    public RouteBuilder schedulerKafkaRoute(ErrorHandlerFactory errorHandlerFactory,
            @Qualifier("schedulerConsumerBuilder")
            EndpointConsumerBuilder scheduler,
            EndpointProducerBuilder kafkaProducerBuilder,
            @Qualifier("afterConsumer")
            ProcessingParams processingAfterConsumerParams,
            @Autowired(required = false)
            @Qualifier("afterProducer")
            ProcessingParams processingAfterProducerParams) {
        return new ConsumerToProducerRoute(errorHandlerFactory,
                                           new ConsumerToProducerRouteParams(GATEWAY_TYPE_SCHEDULER_KAFKA,
                                                                             scheduler,
                                                                             processingAfterConsumerParams,
                                                                             kafkaProducerBuilder,
                                                                             processingAfterProducerParams));
    }
}