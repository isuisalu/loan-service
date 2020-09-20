package org.example.loan.impl;

import org.example.loan.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class LoanController {

    public static final String APPROVAL_PATH = "/approval";
    public static final String APPROVE_PATH = "/approve";
    public static final String APPROVEMENTS_PATH = "/approvements";
    public static final String APPROVED_PATH = "/approved";
    public static final String STATS_PATH = "/stats";

    @Value("${loan.statistics_window}")
    private int statisticsWindow;

    private final Map<String, Map<String, LoanContractApprovement>> pendingLoanApprovements =
            new ConcurrentHashMap<>();
    private final List<LoanContractApprovement> approvedLoanContracts =
            Collections.synchronizedList(new ArrayList<>());

    @PostMapping(APPROVAL_PATH)
    public ResponseEntity<String> sendToApproval(@Valid @RequestBody LoanApprovalRequest request) {

        final Map<String, LoanContractApprovement> loanApprovements = pendingLoanApprovements
                .getOrDefault(request.getLoanContract().getCustomerId(),
                new HashMap<>());
        if (loanApprovements.size() > 0) {
            return ResponseEntity.badRequest()
                    .body("Customer has pending loan request");
        }
        pendingLoanApprovements.put(request.getLoanContract().getCustomerId(), loanApprovements);
        request.getApprovers().forEach(a -> {
            loanApprovements.put(a, new LoanContractApprovement(request.getLoanContract(),
                    a, 0, LoanContractApprovement.State.PENDING));
        });
        return ResponseEntity.ok("Loan is sent for approval");
    }

    @PostMapping(APPROVE_PATH)
    public ResponseEntity<String> approve(@Valid @RequestBody LoanApprovementRequest request) {
        final Map<String, LoanContractApprovement> loanApprovements = pendingLoanApprovements
                .getOrDefault(request.getLoanApprovement().getLoanContract().getCustomerId(),
                        new HashMap<>());
        if (loanApprovements.size() == 0) {
            return ResponseEntity.badRequest()
                    .body("Customer hasn't pending loan request");
        }
        LoanContractApprovement approvement = loanApprovements
                .get(request.getLoanApprovement().getApprover());
        if (approvement == null) {
            return ResponseEntity.badRequest()
                    .body("Nothing to approve");
        }
        approvement.setState(request.getLoanApprovement().getState());
        approvement.setApprovementTime(System.currentTimeMillis());

        if (allChecked(loanApprovements)) {
            approvedLoanContracts.add(approvement);
            pendingLoanApprovements
                    .remove(approvement.getLoanContract().getCustomerId());
            loanApprovements.clear();
            sendApprovedLoanToCustomer(approvement.getLoanContract());
        }
        return ResponseEntity.ok(String.format("Loan approved by %s",
                request.getLoanApprovement().getApprover()));
    }
    @GetMapping(APPROVEMENTS_PATH)
    @ResponseBody
    public List<LoanContractApprovement> getPendingApprovements(@NotNull @RequestParam String approver) {
        final List<LoanContractApprovement> approvements = new ArrayList<>();
        pendingLoanApprovements.values().forEach(a -> {
            LoanContractApprovement approvement = a.get(approver);
            if (approvement != null) {
                approvements.add(approvement);
            }
        });
        return approvements;
    }
    @GetMapping(APPROVED_PATH)
    @ResponseBody
    public List<LoanContractApprovement> getApprovedLoanContracts(@NotNull @RequestParam String customerId) {
        return approvedLoanContracts.stream()
            .filter(l -> l.getLoanContract().getCustomerId().equals(customerId))
                .collect(Collectors.toList());
     }

    @GetMapping(STATS_PATH)
    @ResponseBody
    public LoanStatistics computeStats() {
        long statsBegin = System.currentTimeMillis() - 1000 * statisticsWindow;
        List<LoanContractApprovement> samples = new ArrayList<>();
        for (int i = approvedLoanContracts.size() - 1;i >= 0;i--) {
            LoanContractApprovement loanApprovement = approvedLoanContracts.get(i);

            if (loanApprovement.getApprovementTime() >= statsBegin) {
                samples.add(loanApprovement);
            } else break;
        }
        return new LoanStatistics(samples.stream().map(a -> a.getLoanContract().getAmount())
            .collect(Collectors.toList()));
    }
    private boolean allChecked(Map<String, LoanContractApprovement> loanApprovements) {
        return loanApprovements.values().stream().noneMatch(a ->
                LoanContractApprovement.State.PENDING.compareTo(a.getState()) == 0);
    }
    private void sendApprovedLoanToCustomer(LoanContract loan) {

    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
