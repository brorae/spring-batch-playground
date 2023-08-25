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
public class StepNextConditionalJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job stepNextConditionalJob() {
        return new JobBuilder("stepNextConditionalJob", jobRepository)
                .start(conditionalJobStep1())
                .on("FAILED")
                .to(conditionalJobStep3())
                .on("*")
                .end()
                .from(conditionalJobStep1())
                .on("*")
                .to(conditionalJobStep2())
                .next(conditionalJobStep3())
                .on("*")
                .end()
                .end()
                .build();
    }

    @Bean
    public Step conditionalJobStep1() {
        return new StepBuilder("conditionalJobStep1", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>> This is ConditionalJobStep1");

//                    contribution.setExitStatus(ExitStatus.FAILED);

                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Step conditionalJobStep2() {
        return new StepBuilder("conditionalJobStep2", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>> This is ConditionalJobStep2");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Step conditionalJobStep3() {
        return new StepBuilder("conditionalJobStep3", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>> This is ConditionalJobStep3");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

}
