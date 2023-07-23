package jsh.springbatch.springbatchstudy.domainexamplesource.jobinstance;

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

@Configuration
@RequiredArgsConstructor
public class JobInstanceConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job instanceJob() {
        return jobBuilderFactory.get("instanceJob")
                                .start(instanceStep1())
                                .next(instanceStep2())
                                .build();
    }

    @Bean
    public Step instanceStep1() {
        return stepBuilderFactory.get("instanceStep1")
                                 .tasklet(new Tasklet() {
                                     @Override
                                     public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                                         System.out.println("instanceStep1 was excuted");
                                         return null;
                                     }
                                 })
                                 .build();
    }

    @Bean
    public Step instanceStep2() {
        return stepBuilderFactory.get("instanceStep2")
                                 .tasklet(new Tasklet() {
                                     @Override
                                     public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                                         System.out.println("instanceStep2 was excuted");
                                         return null;
                                     }
                                 })
                                 .build();
    }
}
