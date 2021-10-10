package com.kodilla.task.batch.kodillabatchtask;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    FlatFileItemReader<UserInput> reader() {
        FlatFileItemReader<UserInput> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("users_input.csv"));

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("name", "surname", "bday");

        BeanWrapperFieldSetMapper<UserInput> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(UserInput.class);

        DefaultLineMapper<UserInput> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        reader.setLineMapper(lineMapper);
        reader.setLinesToSkip(1);
        return reader;
    }

    @Bean
    UserInputProcessor processor() {
        return new UserInputProcessor();
    }

    @Bean
    FlatFileItemWriter<UserOutput> writer() {
        String[] names = new String[] {"name", "surname", "ageInYears"};
        BeanWrapperFieldExtractor<UserOutput> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(names);

        DelimitedLineAggregator<UserOutput> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);

        FlatFileItemWriter<UserOutput> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("users_output.csv"));
        writer.setHeaderCallback(
                writer1 -> writer1.write(Arrays.toString(names).replaceAll("\\[", "").replaceAll("]", ""))
        );
        writer.setShouldDeleteIfExists(true);
        writer.setLineAggregator(aggregator);

        return writer;
    }

    @Bean
    Step calculateAge(
            ItemReader<UserInput> reader,
            ItemProcessor<UserInput, UserOutput> processor,
            ItemWriter<UserOutput> writer) {
        return stepBuilderFactory.get("calculateAge")
                .<UserInput, UserOutput>chunk(100)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    //Końcowym etapem jest skonfigurowanie całego procesu, czyli joba
    @Bean
    Job changePriceJob(Step priceChange) {
        return jobBuilderFactory.get("changePriceJob")
                .incrementer(new RunIdIncrementer())
                .flow(priceChange)
                .end()
                .build();
    }

}
