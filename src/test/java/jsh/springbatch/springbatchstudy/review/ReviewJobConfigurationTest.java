package jsh.springbatch.springbatchstudy.review;

import jsh.springbatch.springbatchstudy.TestBatchConfig;
import jsh.springbatch.springbatchstudy.review.job.StoreJobConfiguration;
import jsh.springbatch.springbatchstudy.review.job.chunk.writer.InquiryTargetItemWriter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes={StoreJobConfiguration.class, TestBatchConfig.class})
public class ReviewJobConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @MockBean
    private InquiryTargetItemWriter inquiryTargetItemWriter;

    @Test
    public void job_test() throws Exception {

    }

    @Test
    public void step_test() throws Exception {

        // given

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("inquiryTargetStep");
        StepExecution stepExecution = (StepExecution)((List)jobExecution.getStepExecutions()).get(0);

        // then
        assertEquals(stepExecution.getStatus(), BatchStatus.COMPLETED);
        assertEquals(stepExecution.getExitStatus(), BatchStatus.COMPLETED);
    }
}
