package com.eex.application.controller;

import com.eex.domain.entity.Account;
import com.eex.domain.entity.Client;
import com.eex.domain.entity.User;
import com.eex.domain.service.AccountService;
import com.eex.domain.service.ClientService;
import com.eex.domain.service.UserService;
import com.eex.domain.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TenantIsolationAndAccountRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AccountService accountService;

    @AfterEach
    void tearDown() {
        // Reset account 1's name to AMAZON to prevent polluting other test runs
        accountService.getAccountById(1).ifPresent(acc -> {
            Account reset = new Account();
            reset.setName("AMAZON");
            accountService.updateAccount(1, reset);
        });
    }

    @Test
    void userCanOnlySeeOwnAccounts() throws Exception {
        // Alice has userId=1, clientId=1
        String aliceToken = jwtUtil.generateToken(1, 1);

        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("AMAZON"))
                .andExpect(jsonPath("$[0].clientId").value(1));
    }

    @Test
    void userCannotAccessOtherClientAccountsDirectly() throws Exception {
        // Alice has userId=1, clientId=1. Account 2 belongs to client 2 (Bernard).
        String aliceToken = jwtUtil.generateToken(1, 1);

        // Accessing account 2 by id directly
        mockMvc.perform(get("/api/accounts/2")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNotFound());

        // Accessing client 2 accounts by clientId route
        mockMvc.perform(get("/api/accounts/client/2")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanOnlySeeOwnClient() throws Exception {
        // Alice has userId=1, clientId=1
        String aliceToken = jwtUtil.generateToken(1, 1);

        mockMvc.perform(get("/api/clients")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(get("/api/clients/2")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCanOnlySeeOwnUsers() throws Exception {
        // Alice has userId=1, clientId=1
        String aliceToken = jwtUtil.generateToken(1, 1);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("Alice"));

        // Searching a user from another client should not return them
        mockMvc.perform(get("/api/users/search")
                        .param("username", "Bernard")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void accountUpdateOnlyModifiesNameAndNeverAmount() throws Exception {
        // Alice has userId=1, clientId=1, owns account 1 with amount 1000.0
        String aliceToken = jwtUtil.generateToken(1, 1);

        String payload = "{\"name\":\"NEW_AMAZON_NAME\",\"amount\":999999.0}";

        mockMvc.perform(put("/api/accounts/1")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NEW_AMAZON_NAME"))
                .andExpect(jsonPath("$.amount").value(1000.0)); // Amount must remain unchanged

        Account updated = accountService.getAccountById(1).orElseThrow();
        assertEquals("NEW_AMAZON_NAME", updated.getName());
        assertEquals(1000.0, updated.getAmount());
    }
}
