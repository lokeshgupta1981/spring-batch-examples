package com.howtodoinjava.demo.writer;

import com.howtodoinjava.demo.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class LoggingPersonWriter implements ItemWriter<Person> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPersonWriter.class);

  @Override
  public void write(Chunk<? extends Person> people) throws Exception {
    LOGGER.info("Writing the information of {} people : {}", people.size(), people.getItems());
  }
}