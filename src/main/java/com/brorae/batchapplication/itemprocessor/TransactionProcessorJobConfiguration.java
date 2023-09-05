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
public class TransactionProcessorJobConfiguration {

    private static final String JOB_NAME = "transactionWriterBatch12";
    private static final String BEAN_PREFIX = JOB_NAME + "_";
    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job transactionProcessorJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .preventRestart()
                .start(transactionProcessorJobStep())
                .build();
    }

    @Bean(BEAN_PREFIX + "step")
    public Step transactionProcessorJobStep() {
        return new StepBuilder(BEAN_PREFIX + "step", jobRepository)
                .<Teacher, ClassInformation>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(transactionProcessorJobReader())
                .processor(transactionProcessorJobProcessor())
                .writer(transactionProcessorJobWriter())
                .build();
    }

    @Bean
    public ItemReader<Teacher> transactionProcessorJobReader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name(BEAN_PREFIX + "reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    @Bean
    public ItemProcessor<Teacher, ClassInformation> transactionProcessorJobProcessor() {
        return teacher -> new ClassInformation(teacher.getName(), teacher.getStudents().size());
    }


    @Bean
    public ItemWriter<ClassInformation> transactionProcessorJobWriter() {
        return items -> {
            for (ClassInformation item : items) {
                log.info("ClassInformation Name={}, Size={}", item.getName(), item.getSize());
            }
        };
    }
}
