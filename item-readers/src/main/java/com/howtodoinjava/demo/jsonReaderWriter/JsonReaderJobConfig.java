package com.howtodoinjava.demo.jsonReaderWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howtodoinjava.demo.model.Person;
import com.howtodoinjava.demo.processor.LoggingPersonProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.SimpleDateFormat;

@Configuration
@SpringBootApplication
public class JsonReaderJobConfig {

  @Value("classpath:person.json")
  private Resource jsonFile;

  @Bean
  Job job(Step step1, JobRepository jobRepository) {

    var builder = new JobBuilder("job", jobRepository);
    return builder
        .start(step1)
        .build();
  }

  @Bean
  public Step step1(JsonItemReader<Person> reader,
                    JsonFileItemWriter<Person> writer,
                    JobRepository jobRepository,
                    PlatformTransactionManager txManager) {

    var builder = new StepBuilder("step1", jobRepository);
    return builder
        .<Person, Person>chunk(1, txManager)
        .reader(reader)
        .processor(new LoggingPersonProcessor())
        .writer(writer)
        .build();
  }

  @Bean
  @StepScope
  public JsonItemReader<Person> personJsonItemReader() {

    /*ObjectMapper objectMapper = new ObjectMapper();
    //customize objectMapper if needed
    JacksonJsonObjectReader<Person> jsonObjectReader = new JacksonJsonObjectReader<>(Person.class);
    jsonObjectReader.setMapper(objectMapper);*/

    return new JsonItemReaderBuilder<Person>()
        .name("personJsonItemReader")
        .jsonObjectReader(new JacksonJsonObjectReader<>(Person.class))
        .resource(jsonFile)
        .build();
  }

  @Bean
  public JsonFileItemWriter<Person> personJsonFileItemWriter() {

    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    WritableResource outputJsonFile = (WritableResource) resolver.getResource("file:output-person.json");

    return new JsonFileItemWriterBuilder<Person>()
        .name("personJsonFileItemWriter")
        .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
        .resource(outputJsonFile)
        .build();

  }

  public static void main(String[] args) {
    SpringApplication.run(JsonReaderJobConfig.class);
  }
}
