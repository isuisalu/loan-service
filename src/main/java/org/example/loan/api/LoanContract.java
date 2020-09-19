package org.example.loan.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class LoanContract {
    @NotEmpty
    private String customerId;
    @NotNull
    private BigDecimal amount;
}
