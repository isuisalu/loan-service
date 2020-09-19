package org.example.loan.api;

import lombok.Value;

import java.math.BigDecimal;
import java.util.Comparator;
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
                .reduce(ZERO, (a, b) -> a.add(b));
        this.average = this.count > 0 ? this.sum.divide(new BigDecimal((long)this.count)) :
                ZERO;
        this.min = loanApprovementAmounts.stream()
                .min((o1, o2) -> o1.compareTo(o2)).get();
        this.max = loanApprovementAmounts.stream()
                .max((o1, o2) -> o1.compareTo(o2)).get();
    }
}
