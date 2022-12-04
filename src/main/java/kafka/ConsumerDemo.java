package kafka;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerDemo {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class);

    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

    public static void main(String[] args) {
        log.info("I'm a Kafka Consumer");

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "my-fourth-application");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            Thread mainThread = Thread.currentThread();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
                consumer.wakeup();
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

            try {
                consumer.subscribe(List.of("demo_java"));
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    records.forEach(r -> {
                        log.info("Key: %s, Value: %s".formatted(r.key(), r.value()));
                        log.info("Partition: %s, Offset: %s".formatted(r.partition(), r.offset()));
                    });
                }
            } catch (WakeupException e) {
                log.info("Wake up exception!");
            } catch (Exception e) {
                log.error("Unexpected exception", e);
            } finally {
                consumer.close();
                log.info("The consumer's been gracefully closed.");
            }
        }
    }

}
