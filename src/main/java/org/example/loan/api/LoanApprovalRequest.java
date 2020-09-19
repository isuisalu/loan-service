package org.example.loan.api;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Value
public class LoanApprovalRequest {
    @Valid
    private LoanContract loanContract;
    @NotEmpty
    private List<String> approvers;
}
