package org.example.loan.api;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Value
public class LoanStatistics {
    public static final BigDecimal ZERO = new BigDecimal(0);

    private int count;
    private BigDecimal sum;
    private BigDecimal average;
    private BigDecimal min;
    private BigDecimal max;

    public LoanStatistics(List<BigDecimal> loanApprovementAmounts) {
        this.count = loanApprovementAmounts.size();
        this.sum = loanApprovementAmounts.stream()
                .reduce(ZERO, BigDecimal::add);
        this.average = this.count > 0 ?
                this.sum.divide(new BigDecimal((long)this.count),
                        RoundingMode.HALF_UP) :
                ZERO;
        this.min = loanApprovementAmounts.stream()
                .min(BigDecimal::compareTo).get();
        this.max = loanApprovementAmounts.stream()
                .max(BigDecimal::compareTo).get();
    }
}
