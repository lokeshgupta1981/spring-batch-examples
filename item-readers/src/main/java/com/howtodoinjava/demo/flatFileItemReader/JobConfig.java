package com.howtodoinjava.demo.flatFileItemReader;

import com.howtodoinjava.demo.model.Person;
import com.howtodoinjava.demo.processor.LoggingPersonProcessor;
import com.howtodoinjava.demo.writer.LoggingPersonWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@SpringBootApplication
public class JobConfig {

  @Value("classpath:person.csv")
  private Resource csvFile;

  @Bean
  Job job(Step step1, JobRepository jobRepository) {

    var builder = new JobBuilder("job", jobRepository);
    return builder
        .start(step1)
        .build();
  }

  @Bean
  public Step step1(ItemReader<Person> reader,
                    JobRepository jobRepository,
                    PlatformTransactionManager txManager) {

    var builder = new StepBuilder("step1", jobRepository);
    return builder
        .<Person, Person>chunk(1, txManager)
        .reader(reader)
        //.processor(new LoggingPersonProcessor())  // It is optional
        .writer(new LoggingPersonWriter())
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<Person> personItemReader() {
    return new FlatFileItemReaderBuilder<Person>()
        .name("personItemReader")
        .delimited()
        .names("firstName", "lastName", "age", "active")
        .targetType(Person.class)
        .resource(csvFile)
        .build();
  }

  /*@Bean
  @StepScope
  public FlatFileItemReader<Person> personItemReaderFixedWidth() {
    return new FlatFileItemReaderBuilder<Person>()
        .name("personItemReader")
        .fixedLength()
        .columns(new Range(1, 10), new Range(11, 20), new Range(21, 24), new Range(25, 30))
        .names("firstName", "lastName", "age", "active")
        .targetType(Person.class)
        .resource(csvFile)
        .build();
  }*/

  /*@Bean
  public FlatFileItemReader<Person> reader() {

    FlatFileItemReader<Person> reader = new FlatFileItemReader<>();

    reader.setResource(csvFile);

    reader.setLineMapper(new DefaultLineMapper<>() {{
      setLineTokenizer(new DelimitedLineTokenizer() {{
        setDelimiter("#");
        setNames("firstName", "lastName", "age", "active");
      }});
      setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
        setTargetType(Person.class);
      }});
    }});

    return reader;
  }*/

  public static void main(String[] args) {
    SpringApplication.run(JobConfig.class);
  }
}
