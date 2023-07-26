package routetest.kafkasql;

import com.bridle.App;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static com.bridle.configuration.routes.KafkaHttpConfiguration.GATEWAY_TYPE_KAFKA_HTTP;
import static com.bridle.configuration.routes.KafkaSqlConfiguration.GATEWAY_TYPE_KAFKA_SQL;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.testcontainers.containers.KafkaContainer.KAFKA_PORT;
import static utils.KafkaContainerUtils.createKafkaContainer;
import static utils.KafkaContainerUtils.createTopic;
import static utils.KafkaContainerUtils.setupKafka;
import static utils.KafkaContainerUtils.writeMessageToTopic;
import static utils.MetricsTestUtils.verifyMetrics;
import static utils.MockServerContainerUtils.createMockServerClient;
import static utils.MockServerContainerUtils.createMockServerContainer;
import static utils.OracleContainerUtils.createOracleContainer;
import static utils.TestUtils.getStringResources;


@SpringBootTest(classes = {App.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:routetest/kafka-sql/application.yml"})
@CamelSpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureMetrics
public class KafkaSqlRouteTest {

    private static final String TOPIC_NAME = "routetest";

    @Container
    private static final KafkaContainer kafka = createKafkaContainer();

    @Container
    public static OracleContainer oracle = createOracleContainer().withInitScript("routetest/kafka-sql/init.sql");
    private final static String MESSAGE_IN_KAFKA = getStringResources("routetest/kafka-sql/test.json");
    @Autowired
    private CamelContext context;

    @Autowired
    private ProducerTemplate producerTemplate;

    @BeforeAll
    public static void setUp() throws Exception {
        setupKafka(kafka, KAFKA_PORT);
        System.setProperty("components.kafka.kafka-in.brokers", "localhost:" + kafka.getMappedPort(KAFKA_PORT).toString());

        oracle.start();
        System.setProperty("datasources.hikari.main-datasource.jdbc-url", oracle.getJdbcUrl());
    }

    @AfterAll
    public static void afterAll() throws Exception {
        oracle.stop();
    }

    @Test
    void verifyThatRouteReadMessagesFromTopicAndInvokeHttpEndpoint() throws Exception {
        int messageCount = 1;
        createTopic(kafka, TOPIC_NAME);
        NotifyBuilder notify = new NotifyBuilder(context).whenExactlyCompleted(messageCount).create();

        writeMessageToTopic(kafka, TOPIC_NAME, MESSAGE_IN_KAFKA);

        boolean done = notify.matches(10, TimeUnit.SECONDS);
        Assertions.assertTrue(done);
        verifyMetrics(GATEWAY_TYPE_KAFKA_SQL, messageCount, 0, 0);
    }
}

