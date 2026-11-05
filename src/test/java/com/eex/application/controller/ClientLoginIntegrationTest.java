package com.eex.application.controller;

import com.eex.domain.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClientLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void whenLoginWithClientIdDirectly_shouldReturnTokenAndAllowAccess() throws Exception {
        String payload = "{\"clientId\": 2}";

        String response = mockMvc.perform(post("/api/auth/client-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        // Extract token
        String token = response.split("\"token\":\"")[1].split("\"")[0];

        // Should see client 2's accounts (BORNEO)
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("BORNEO"))
                .andExpect(jsonPath("$[0].clientId").value(2));

        // Should see client 2's data
        mockMvc.perform(get("/api/clients")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void whenLoginWithClientIdAndUserCredentials_shouldReturnToken() throws Exception {
        String payload = "{\"clientId\": 1, \"username\": \"Alice\", \"password\": \"aliceSecret\"}";

        mockMvc.perform(post("/api/auth/client-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void whenLoginWithNonExistentClientId_shouldReturn401() throws Exception {
        String payload = "{\"clientId\": 999}";

        mockMvc.perform(post("/api/auth/client-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }
}

