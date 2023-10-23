package jsh.springbatch.springbatchstudy.review.job.chunk.writer;

import jsh.springbatch.springbatchstudy.review.job.domain.ReviewableCountDTO;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InquiryTargetItemWriter implements ItemWriter<ReviewableCountDTO> {
    @Override
    public void write(List<? extends ReviewableCountDTO> list) throws Exception {

    }
}
