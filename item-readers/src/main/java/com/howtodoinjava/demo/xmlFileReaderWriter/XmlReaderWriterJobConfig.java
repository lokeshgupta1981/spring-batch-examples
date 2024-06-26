package com.howtodoinjava.demo.xmlFileReaderWriter;

import com.howtodoinjava.demo.model.Person;
import com.howtodoinjava.demo.processor.LoggingPersonProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@SpringBootApplication
@SuppressWarnings("unused")
public class XmlReaderWriterJobConfig {

  @Value("classpath:person.xml")
  private Resource xmlFile;

  @Bean
  Job job(Step step1, JobRepository jobRepository) {

    var builder = new JobBuilder("job", jobRepository);
    return builder
        .start(step1)
        .build();
  }

  @Bean
  public Step step1(StaxEventItemReader<Person> reader,
                    StaxEventItemWriter<Person> writer,
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
  public StaxEventItemReader<Person> personXmlFileReader() {
    return new StaxEventItemReaderBuilder<Person>()
        .name("personXmlFileReader")
        .resource(xmlFile)
        .addFragmentRootElements("person")
        .unmarshaller(personMarshaller())
        .build();
  }

  @Bean
  public StaxEventItemWriter<Person> personXmlFileWriter() {

    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    WritableResource outputXml = (WritableResource) resolver.getResource("file:output-person.xml");

    return new StaxEventItemWriterBuilder<Person>()
        .name("personXmlFileWriter")
        .marshaller(personMarshaller())
        .resource(outputXml)
        .rootTagName("people")
        .overwriteOutput(true)
        .build();

  }

  @Bean
  public Jaxb2Marshaller personMarshaller() {
    Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    jaxb2Marshaller.setClassesToBeBound(Person.class);
    return jaxb2Marshaller;
  }

  public static void main(String[] args) {
    SpringApplication.run(XmlReaderWriterJobConfig.class);
  }
}
