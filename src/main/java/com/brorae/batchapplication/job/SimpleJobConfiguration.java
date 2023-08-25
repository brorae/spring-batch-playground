package com.brorae.batchapplication.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final SimpleJobTasklet simpleJobTasklet;

    @Bean
    public Job simpleJob1() {
        return new JobBuilder("simpleJob1", jobRepository)
                .start(simpleStep())
                .next(simpleStep1())
                .build();
    }

    @Bean
    public Step simpleStep() {
        return new StepBuilder("simpleStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>> This is SimpleStep");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Step simpleStep1() {
        return new StepBuilder("simpleStep1", jobRepository)
                .tasklet(simpleJobTasklet, platformTransactionManager)
                .build();
    }
}
