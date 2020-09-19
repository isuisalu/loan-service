package org.example.loan.impl;

import com.google.gson.Gson;
import org.example.loan.api.LoanApprovalRequest;
import org.example.loan.api.LoanApprovementRequest;
import org.example.loan.api.LoanContract;
import org.example.loan.api.LoanContractApprovement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.example.loan.api.LoanContractApprovement.State.APPROVED;
import static org.example.loan.impl.LoanController.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class LoanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static final String CUSTOMER_ID = UUID.randomUUID().toString();
    private static final String APPROVER1 = "approver1";
    private static final String APPROVER2 = "approver2";
    private static final String APPROVER3 = "approver3";
    private static final BigDecimal AMOUNT1 = new BigDecimal(10.);
    private static final BigDecimal AMOUNT2 = new BigDecimal(20.);

    @Test
    public void whenMissingAmount_thenBadRequest() throws Exception {
        beginApproval(null, status().isBadRequest(),
                content().json("{'loanContract.amount':'must not be null'}"));

    }
    @Test
    public void testApprovement() throws Exception {
        runApprovement(AMOUNT1);
        mockMvc.perform(get(APPROVED_PATH)
                .param("customerId", CUSTOMER_ID)
        ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].loanContract.customerId").value(CUSTOMER_ID))
                .andExpect(jsonPath("$[0].state").value(APPROVED.name()));
    }
    @Test
    public void testApprovementStats() throws Exception {
        runApprovement(AMOUNT1);
        runApprovement(AMOUNT2);
        mockMvc.perform(get(STATS_PATH)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.sum").value(30))
                .andExpect(jsonPath("$.average").value(15))
                .andExpect(jsonPath("$.min").value(10))
                .andExpect(jsonPath("$.max").value(20));
    }

    private void runApprovement(BigDecimal amount) throws Exception {
        beginApproval(amount, status().isOk());
        approve(APPROVER1);
        approve(APPROVER2);
        approve(APPROVER3);
    }
    private void beginApproval(BigDecimal amount, ResultMatcher ... mathers)  throws Exception {
        LoanContract loan = new LoanContract(CUSTOMER_ID,
                amount);

        LoanApprovalRequest request = new LoanApprovalRequest(loan,
                Arrays.asList(APPROVER1, APPROVER2, APPROVER3));

        Gson gson = new Gson();
        String json = gson.toJson(request);

        ResultActions actions = mockMvc.perform(post(LoanController.APPROVAL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
        for (ResultMatcher matcher : mathers) {
            actions = actions.andExpect(matcher);
        }
    }

    private void approve(String approver) throws Exception {
        MvcResult result = mockMvc.perform(get(APPROVEMENTS_PATH)
                .param("approver", approver)
        ).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        Gson gson = new Gson();

        LoanContractApprovement[] approvements = gson.fromJson(json, LoanContractApprovement[].class);
        assertTrue(approvements.length == 1);

        LoanContractApprovement approvement = approvements[0];

        assertTrue(approvement.getApprover().equals(approver));
        assertTrue(approvement.getApprovementTime() == 0);
        assertTrue(approvement.getState()
                .compareTo(LoanContractApprovement.State.PENDING) == 0);

        approvement.setState(APPROVED);

        json = gson.toJson(new LoanApprovementRequest(approvement));
        mockMvc.perform(post(LoanController.APPROVE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    public void testOnlyOnePendingApproval() {

    }
}
