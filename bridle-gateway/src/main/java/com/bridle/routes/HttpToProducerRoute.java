package com.bridle.routes;

import com.bridle.properties.HttpConsumerConfiguration;
import org.apache.camel.ErrorHandlerFactory;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.model.ConvertBodyDefinition;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.RouteDefinition;

import static com.bridle.configuration.common.ComponentNameConstants.REDELIVERY_POLICY;
import static com.bridle.configuration.routes.HttpKafkaConfiguration.GATEWAY_TYPE_HTTP_KAFKA;
import static org.apache.camel.component.rest.RestConstants.CONTENT_TYPE;
import static org.apache.camel.component.rest.RestConstants.HTTP_RESPONSE_CODE;

public class HttpToProducerRoute extends GenericHttpConsumerRoute {

    private final EndpointProducerBuilder mainProducer;
    private final EndpointProducerBuilder successResponseBuilder;
    private final EndpointProducerBuilder errorResponseBuilder;
    private final EndpointProducerBuilder transform;
    private final Processor headerCollector;
    private final ConvertBodyDefinition convertBodyDefinition;
    private final DataFormatDefinition beforeTransformDataFormatDefinition;
    private final DataFormatDefinition afterTransformDataFormatDefinition;

    public HttpToProducerRoute(ErrorHandlerFactory errorHandlerFactory,
                               HttpConsumerConfiguration restConfiguration,
                               EndpointProducerBuilder mainProducer,
                               EndpointProducerBuilder successResponseBuilder,
                               EndpointProducerBuilder errorResponseBuilder,
                               EndpointProducerBuilder transform,
                               Processor headerCollector,
                               ConvertBodyDefinition convertBody,
                               DataFormatDefinition beforeTransformDataFormatDefinition,
                               DataFormatDefinition afterTransformDataFormatDefinition) {
        super(errorHandlerFactory, restConfiguration);
        this.mainProducer = mainProducer;
        this.successResponseBuilder = successResponseBuilder;
        this.errorResponseBuilder = errorResponseBuilder;
        this.transform = transform;
        this.headerCollector = headerCollector;
        this.convertBodyDefinition = convertBody;
        this.beforeTransformDataFormatDefinition = beforeTransformDataFormatDefinition;
        this.afterTransformDataFormatDefinition = afterTransformDataFormatDefinition;
    }

    @Override
    public void configure() throws Exception {
        super.configure();

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Exception occurred: ${exception.stacktrace}")
                .handled(true)
                .redeliveryPolicyRef(REDELIVERY_POLICY)
                .setHeader(HTTP_RESPONSE_CODE, constant(restConfiguration.getErrorHttpResponseCode()))
                .to(errorResponseBuilder)
                .log("Response: ${body}");

        RouteDefinition routeDefinition = from("direct:process")
                .routeId(GATEWAY_TYPE_HTTP_KAFKA)
                .setHeader(CONTENT_TYPE, constant(restConfiguration.getContentType()));

        if (convertBodyDefinition != null) {
            routeDefinition.addOutput(convertBodyDefinition);
        }

        if (headerCollector != null) {
            routeDefinition.process(headerCollector);
        }

        if (beforeTransformDataFormatDefinition != null) {
            routeDefinition.unmarshal(beforeTransformDataFormatDefinition);
        }

        if (transform != null) {
            routeDefinition.to(transform);
        }

        if (afterTransformDataFormatDefinition != null) {
            routeDefinition.marshal(afterTransformDataFormatDefinition);
        }

        if (mainProducer != null) {
            routeDefinition.to(mainProducer);
        }

        routeDefinition
                .to(successResponseBuilder)
                .log("Response: ${body}");
    }
}
