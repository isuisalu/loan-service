package org.example.loan.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovementRequest {
    @Valid
    private LoanContractApprovement loanApprovement;
}
