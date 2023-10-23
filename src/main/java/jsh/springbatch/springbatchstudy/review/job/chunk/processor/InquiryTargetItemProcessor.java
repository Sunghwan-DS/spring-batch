package jsh.springbatch.springbatchstudy.review.job.chunk.processor;

import jsh.springbatch.springbatchstudy.review.job.domain.ReviewableCountDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class InquiryTargetItemProcessor implements ItemProcessor<ReviewableCountDTO, ReviewableCountDTO> {
    @Override
    public ReviewableCountDTO process(ReviewableCountDTO reviewableCountDTO) throws Exception {
        return null;
    }
}
