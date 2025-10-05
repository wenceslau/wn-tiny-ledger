package com.wn.tiny.ledger.infrastructure.controller.dto;

import com.wn.tiny.ledger.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        @NotNull(message = "Transaction type cannot be null")
        TransactionType type) {
}
