package com.brorae.batchapplication.itemprocessor;


import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ProcessorConvertJobConfiguration {

    private static final String JOB_NAME = "processorConvertBatch";
    private static final String BEAN_PREFIX = JOB_NAME + "_";

    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job processorConvertJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .preventRestart()
                .start(processConvertJobStep())
                .build();
    }

    @Bean(BEAN_PREFIX + "step")
    public Step processConvertJobStep() {
        return new StepBuilder(BEAN_PREFIX + "step", jobRepository)
                .<Teacher, String>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(processConvertJobReader())
                .processor(processConvertJobProcessor())
                .writer(processConvertJobWriter())
                .build();
    }

    @Bean
    public ItemReader<Teacher> processConvertJobReader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name(BEAN_PREFIX + "reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    @Bean
    public ItemProcessor<Teacher, String> processConvertJobProcessor() {
        return teacher -> teacher.getName();
    }

    @Bean
    public ItemWriter<String> processConvertJobWriter() {
        return items -> {
            for (String item : items) {
                log.info("Teacher Name={}", item);
            }
        };
    }
}
