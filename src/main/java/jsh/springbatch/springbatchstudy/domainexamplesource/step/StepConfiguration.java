package jsh.springbatch.springbatchstudy.domainexamplesource.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class StepConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job stepJob() {
        return jobBuilderFactory.get("stepJob")
                                .start(stepJobStep1())
                                .next(stepJobStep2())
                                .next(stepJobStep3())
                                .build();
    }

    @Bean
    public Step stepJobStep1() {
        return stepBuilderFactory.get("stepJobStep1")
                                 .tasklet(new CustomTasklet())
                                 .build();
    }

    @Bean
    public Step stepJobStep2() {
        return stepBuilderFactory.get("stepJobStep2")
                                 .tasklet((stepContribution, chunkContext) -> {
                                     System.out.println("stepJobStep2 has executed");
                                     throw new RuntimeException("stepJobStep2 has failed");
//                                     return RepeatStatus.FINISHED;
                                 })
                                 .build();
    }

    @Bean
    public Step stepJobStep3() {
        return stepBuilderFactory.get("stepJobStep3")
                                 .tasklet((stepContribution, chunkContext) -> {
                                     System.out.println("stepJobStep3 has executed");
                                     return RepeatStatus.FINISHED;
                                 })
                                 .build();
    }
}
