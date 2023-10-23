package jsh.springbatch.springbatchstudy.review.job;

import jsh.springbatch.springbatchstudy.review.job.chunk.processor.InquiryTargetItemProcessor;
import jsh.springbatch.springbatchstudy.review.job.chunk.reader.InquiryTargetItemReader;
import jsh.springbatch.springbatchstudy.review.job.chunk.writer.InquiryTargetItemWriter;
import jsh.springbatch.springbatchstudy.review.job.domain.ReviewableCountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StoreJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final InquiryTargetItemWriter inquiryTargetItemWriter;

    @Bean
    public Job StoreJob() {
        return jobBuilderFactory.get("storeJob")
                                .start(inquiryTargetStep())
                                .incrementer(new RunIdIncrementer())
                                .build();
    }

    @Bean
    public Step inquiryTargetStep() {
        return stepBuilderFactory.get("inquiryTargetStep")
                                 .<ReviewableCountDTO, ReviewableCountDTO>chunk(10)
                                 .reader(inquiryTargetItemReader())
                                 .processor(inquiryTargetItemProcessor())
                                 .writer(inquiryTargetItemWriter)
                                 .build();
    }

    @Bean
    public ItemReader<ReviewableCountDTO> inquiryTargetItemReader() {
        return new InquiryTargetItemReader();
    }

    @Bean
    public ItemProcessor<ReviewableCountDTO, ReviewableCountDTO> inquiryTargetItemProcessor() {
        return new InquiryTargetItemProcessor();
    }

    @Bean
    public ItemWriter<ReviewableCountDTO> inquiryTargetItemWriter() {
        return new InquiryTargetItemWriter();
    }
}
