package org.example.loan.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Setter
@Getter
@AllArgsConstructor
public class LoanContractApprovement {

    @Valid
    private LoanContract loanContract;
    @NotEmpty
    private String approver;
    long approvementTime;
    private State state;

    public enum State {
        PENDING, APPROVED, REJECTED;
    }
}
