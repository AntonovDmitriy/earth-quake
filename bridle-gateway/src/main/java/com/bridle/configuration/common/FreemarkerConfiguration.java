package com.bridle.configuration.common;

import com.bridle.properties.FreemarkerProducerConfiguration;
import com.bridle.utils.ComponentCustomizerImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.component.freemarker.FreemarkerComponent;
import org.apache.camel.spi.ComponentCustomizer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import static com.bridle.configuration.common.ComponentNameConstants.FREEMARKER_COMPONENT_NAME;
import static com.bridle.configuration.common.ComponentNameConstants.SUCCESS_RESPONSE_FREEMARKER_COMPONENT_NAME;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.freemarker;

public class FreemarkerConfiguration {

    @ConfigurationProperties(prefix = FREEMARKER_COMPONENT_NAME)
    @ConditionalOnProperty(value = FREEMARKER_COMPONENT_NAME + ".resource-uri")
    @Bean
    public FreemarkerProducerConfiguration freemarkerConfiguration() {
        FreemarkerProducerConfiguration configuration = new FreemarkerProducerConfiguration();
        if (StringUtils.isBlank(configuration.getResourceUri())) {
            configuration.setResourceUri("classpath:transform.tmpl");
        }
        return configuration;
    }

    @Bean(name = FREEMARKER_COMPONENT_NAME)
    @ConditionalOnProperty(value = FREEMARKER_COMPONENT_NAME + ".resource-uri")
    public FreemarkerComponent freemarkerComponent() {
        return new FreemarkerComponent();
    }

    @Bean
    @ConditionalOnProperty(value = FREEMARKER_COMPONENT_NAME + ".resource-uri")
    public EndpointProducerBuilder freemarkerTransformBuilder(@Qualifier("freemarkerConfiguration")
                                                              FreemarkerProducerConfiguration configuration) {
        EndpointProducerBuilder result = freemarker(FREEMARKER_COMPONENT_NAME,
                configuration.getResourceUri());
        configuration.getEndpointProperties()
                .ifPresent(additional -> additional.forEach(result::doSetProperty));
        return result;
    }

    @Lazy
    @Bean
    @ConditionalOnProperty(value = FREEMARKER_COMPONENT_NAME + ".resource-uri")
    public ComponentCustomizer configureFreemarkerComponent(CamelContext context,
                                                            @Qualifier("freemarkerConfiguration")
                                                            FreemarkerProducerConfiguration componentConfiguration) {
        return new ComponentCustomizerImpl(context, componentConfiguration, FREEMARKER_COMPONENT_NAME);
    }

}
