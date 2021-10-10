package com.kodilla.task.batch.kodillabatchtask;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.springframework.batch.item.ItemProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserInputProcessor implements ItemProcessor<UserInput, UserOutput> {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyy-M-d");

    @Override
    public UserOutput process(UserInput input) throws Exception {
        log.debug("Processing user input: {}", input);
        return new UserOutput(input.getName(), input.getSurname(), getAge(input.getBday()));
    }

    private Integer getAge(String bday) {
        if(Objects.isNull(bday)) {
            return null;
        }
        try {
            LocalDate birthday = LocalDate.parse(bday, dateFormatter);
            return birthday.isAfter(LocalDate.now()) ? null : Period.between(birthday, LocalDate.now()).getYears();
        } catch(Exception e) {
            log.error("Error when calculating age: {date: {}, error: {}}", bday, e.getLocalizedMessage());
            return null;
        }
    }
}
