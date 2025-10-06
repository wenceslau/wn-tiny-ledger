package com.wn.tiny.ledger.infrastructure.controller.dto;

import com.wn.tiny.ledger.domain.Transaction;
import com.wn.tiny.ledger.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String reference,
        BigDecimal amount,
        TransactionType type,
        LocalDateTime timestamp
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTimestamp()
        );
    }
}
