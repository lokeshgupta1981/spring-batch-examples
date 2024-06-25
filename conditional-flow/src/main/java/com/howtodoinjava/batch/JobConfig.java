package com.howtodoinjava.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
@Configuration
public class JobConfig {

  JobRepository jobRepository;
  PlatformTransactionManager transactionManager;

  public JobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  @Bean
  Job job(Step firstStep, Step stepOnFailure, Step stepOnSuccess) {

    var builder = new JobBuilder("job", jobRepository);
    return builder
        .start(firstStep)
        .on("FAILED").to(stepOnFailure)
        .from(firstStep).on("*").to(stepOnSuccess)
        .end()
        .build();
  }

  @Bean
  Step firstStep() {

    return new StepBuilder("step1", jobRepository)
        .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
          System.out.println("Printing message from step 1");
          // You can add any additional logic here if needed
          //return RepeatStatus.FINISHED;
          throw new RuntimeException("This is a failure");
        }, transactionManager).build();
  }

  @Bean
  Step stepOnFailure() {

    return new StepBuilder("stepOnFailure", jobRepository)
        .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
          System.out.println("Printing message from step 2");
          // You can add any additional logic here if needed
          return RepeatStatus.FINISHED;
        }, transactionManager).build();
  }

  @Bean
  Step stepOnSuccess() {

    return new StepBuilder("stepOnSuccess", jobRepository)
        .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
          System.out.println("Printing message from step 3");
          // You can add any additional logic here if needed
          return RepeatStatus.FINISHED;
        }, transactionManager).build();
  }


  //Run Method
  public static void main(String[] args) {
    SpringApplication.run(JobConfig.class, args);
  }
}
