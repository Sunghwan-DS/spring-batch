package jsh.springbatch.springbatchstudy;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
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
                                     return RepeatStatus.FINISHED;
                                 })
                                 .build();
    }
}
