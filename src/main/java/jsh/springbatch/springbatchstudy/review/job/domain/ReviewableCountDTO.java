package jsh.springbatch.springbatchstudy.review.job.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewableCountDTO {

    private long memberNo;
    private long confirmCnt;
    private long reviewCnt;
    private long reviewCntExceptEcoupon;
}
