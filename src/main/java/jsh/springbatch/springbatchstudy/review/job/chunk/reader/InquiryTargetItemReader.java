package jsh.springbatch.springbatchstudy.review.job.chunk.reader;

import jsh.springbatch.springbatchstudy.review.job.domain.ReviewableCountDTO;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

@Component
public class InquiryTargetItemReader implements ItemReader<ReviewableCountDTO> {
    @Override
    public ReviewableCountDTO read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        System.out.println("@@@@@@@@@@@@@@ InquiryTargetItemReader!!!");
        return null;
    }
}
