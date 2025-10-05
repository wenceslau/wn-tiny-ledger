package com.wn.tiny.ledger.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wn.tiny.ledger.config.TestSecurityConfig;
import com.wn.tiny.ledger.domain.TransactionType;
import com.wn.tiny.ledger.infrastructure.controller.dto.TransactionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("LedgerController Integration Tests")
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /balance should return initial balance of zero")
    void getBalance_initialState_returnsZero() throws Exception {
        // when / then
        mockMvc.perform(get("/v1/ledger/balance"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.balance", is(0)));
    }

    @Test
    @DisplayName("GET /history should return an empty list initially")
    void getHistory_initialState_returnsEmptyList() throws Exception {
        // when / then
        mockMvc.perform(get("/v1/ledger/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /transactions should create a deposit and update balance")
    void createTransaction_whenDeposit_isSuccessful() throws Exception {
        var request = new TransactionRequest(new BigDecimal("150.50"), TransactionType.DEPOSIT);

        mockMvc.perform(post("/v1/ledger/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", is(150.50)))
                .andExpect(jsonPath("$.type", is("DEPOSIT")));

        mockMvc.perform(get("/v1/ledger/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(150.50)));

        mockMvc.perform(get("/v1/ledger/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("POST /transactions should create a withdrawal and update balance")
    void createTransaction_whenSuccessfulWithdrawal_isSuccessful() throws Exception {
        // given
        var depositRequest = new TransactionRequest(new BigDecimal("200"), TransactionType.DEPOSIT);
        mockMvc.perform(post("/v1/ledger/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)));

        // when
        var withdrawalRequest = new TransactionRequest(new BigDecimal("75"), TransactionType.WITHDRAWAL);
        mockMvc.perform(post("/v1/ledger/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawalRequest)))
                .andExpect(status().isCreated());

        // then
        mockMvc.perform(get("/v1/ledger/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(125)));
    }

    @Test
    @DisplayName("POST /transactions should fail with 400 for withdrawal with insufficient funds")
    void createTransaction_whenInsufficientFunds_returnsBadRequest() throws Exception {
        // given
        var depositRequest = new TransactionRequest(new BigDecimal("50"), TransactionType.DEPOSIT);
        mockMvc.perform(post("/v1/ledger/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)));

        // when
        var withdrawalRequest = new TransactionRequest(new BigDecimal("100"), TransactionType.WITHDRAWAL);
        mockMvc.perform(post("/v1/ledger/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawalRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Insufficient funds for withdrawal")));

        // then
        mockMvc.perform(get("/v1/ledger/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(50)));
    }

    @Test
    @DisplayName("POST /transactions should fail with 400 for invalid request body")
    void createTransaction_whenInvalidInput_returnsBadRequest() throws Exception {
        // when
        var request = new TransactionRequest(new BigDecimal("-100"), TransactionType.DEPOSIT);

        mockMvc.perform(post("/v1/ledger/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.amount[0]", is("Amount must be positive")));
    }

    @Test
    @DisplayName("POST /transactions should fail with 400 for malformed JSON")
    void createTransaction_whenMalformedJson_returnsBadRequest() throws Exception {
        String malformedJson = "{\"amount\": 100, \"type\": \"INVALID_TYPE\"}";

        // when / then
        mockMvc.perform(post("/v1/ledger/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }
}
