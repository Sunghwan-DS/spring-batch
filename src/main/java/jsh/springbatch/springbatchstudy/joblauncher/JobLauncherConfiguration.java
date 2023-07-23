package jsh.springbatch.springbatchstudy.joblauncher;

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
public class JobLauncherConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job JobLauncherJob() {
        return this.jobBuilderFactory.get("JobLauncherJob")
                                     .start(jobLauncherStep1())
                                     .next(jobLauncherStep2()).build();
    }

    @Bean
    public Step jobLauncherStep1() {
        return this.stepBuilderFactory.get("jobLauncherStep1")
                                      .tasklet(new Tasklet() {
                                          @Override
                                          public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                                              return RepeatStatus.FINISHED;
                                          }
                                      })
                                      .build();
    }

    @Bean
    public Step jobLauncherStep2() {
        return this.stepBuilderFactory.get("jobLauncherStep2")
                                      .tasklet(((stepContribution, chunkContext) -> null))
                                      .build();
    }
}
