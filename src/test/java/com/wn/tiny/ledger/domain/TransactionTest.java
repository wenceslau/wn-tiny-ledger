package com.wn.tiny.ledger.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Transaction Domain Tests")
class TransactionTest {

    @Test
    @DisplayName("should create a valid transaction successfully")
    void constructor_withValidArgs_shouldCreateInstance() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        TransactionType type = TransactionType.DEPOSIT;

        // When
        Transaction transaction = new Transaction(amount, type);

        // Then
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getTimestamp()).isNotNull();
        assertThat(transaction.getAmount()).isEqualByComparingTo(amount);
        assertThat(transaction.getType()).isEqualTo(type);
    }

    @Test
    @DisplayName("should throw exception when transaction type is null")
    void constructor_withNullType_shouldThrowException() {
        assertThatThrownBy(() -> new Transaction(new BigDecimal("50"), null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Transaction type must be specified.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-100.00"})
    @NullSource
    @DisplayName("should throw exception for invalid amount (null, zero, or negative)")
    void constructor_withInvalidAmount_shouldThrowException(BigDecimal invalidAmount) {
        assertThatThrownBy(() -> new Transaction(invalidAmount, TransactionType.DEPOSIT))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Transaction amount must be positive.");
    }
}
