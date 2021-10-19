package it.poste.csvkafkaprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import static java.time.ZoneOffset.UTC;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Slf4j
public class CsvKafkaProcessorApplication {

    @Value("${file}")
    String resourceFile;

    @Value("${springHeaderJsonType}")
    String springHeaderJsonType;

    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    private ObjectMapper objectMapper;

    EmitterProcessor<Message<String>> processor = EmitterProcessor.create();

    public static void main(String[] args) {
        SpringApplication.run(CsvKafkaProcessorApplication.class, args);
    }

    @Bean
    public Supplier<Flux<Message<String>>> process() {
        return () -> this.processor;
    }

    @Bean
    CommandLineRunner runner() {
        return args -> {

            initObjectMapper();
            log.info("File is: {}", resourceFile);

            CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build(); // custom separator
            try ( CSVReader reader = new CSVReaderBuilder(
                    new FileReader(loadFile()))
                    .withCSVParser(csvParser)
                    .build()) {

                String[] nextRecord;
                int counter;
                for (counter = 0; (nextRecord = reader.readNext()) != null; counter++) {

                    log.info("payload = {}", nextRecord[1]);
                    log.info("header = {}", nextRecord[0]);
                    Map<String, Object> header = getHeaderMap(nextRecord[0]);
                    Map<String, Object> newheader = new HashMap<>();
                    header.entrySet()
                            .forEach(entry -> {
                                log.info("key = {}, value=\"{}\"", entry.getKey(), entry.getValue());
                                newheader.put(entry.getKey(),"\""+entry.getValue()+"\"");
                            });
                    log.info("spring header json type = {}", springHeaderJsonType);
                    newheader.put("spring_json_header_types", springHeaderJsonType);

                    log.info("=====================");
                    GenericMessage<String> msgtosend = 
                            new GenericMessage<String>(nextRecord[1],newheader);
                    
                    this.processor.onNext(msgtosend);
                }
                log.info("records to send: <{}>", counter);
            }

        };
    }

    private File loadFile()
            throws FileNotFoundException {
        return ResourceUtils.getFile(
                resourceFile);
    }

    private Map<String, Object> getHeaderMap(String header) throws JsonProcessingException {
        return objectMapper.readValue(header, HashMap.class);
    }

    private void initObjectMapper() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        //objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(df);
        objectMapper.setTimeZone(TimeZone.getTimeZone(UTC));
    }

}
