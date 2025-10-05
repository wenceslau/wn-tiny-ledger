package com.wn.tiny.ledger.application;

import com.wn.tiny.ledger.domain.InvalidTransactionException;
import com.wn.tiny.ledger.domain.TransactionType;
import com.wn.tiny.ledger.infrastructure.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("LedgerService Tests")
class LedgerServiceTest {

    @Test
    @DisplayName("should return a valid balance")
    void givenValidValuesToCreateTransaction_whenGetBalance_shouldGetValidBalance() {
        // given
        var ledgerService = new LedgerService(new AccountRepository());
        var amount = new BigDecimal("100");
        var type = TransactionType.DEPOSIT;

        // when / then
        ledgerService.recordTransaction(amount, type);
        assertThat(ledgerService.getBalance()).isEqualByComparingTo(amount.toString());

        amount = new BigDecimal("50");
        type = TransactionType.WITHDRAWAL;

        ledgerService.recordTransaction(amount, type);
        assertThat(ledgerService.getBalance()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("should throw exception Insufficient funds for withdrawal")
    void  givenAmountWithNoFunds_ShouldThrowAnException() {
        // given
        var ledgerService = new LedgerService(new AccountRepository());
        var amount = new BigDecimal("100");
        var type = TransactionType.WITHDRAWAL;

        // when / then
        assertThatThrownBy(() -> ledgerService.recordTransaction(amount, type))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Insufficient funds for withdrawal");

    }

    @Test
    @DisplayName("should return a valid transaction history")
    void givenValidDataToCreateTransaction_whenCallGetTransaction_shouldReturnTheTransaction(){
        // given
        var ledgerService = new LedgerService(new AccountRepository());
        var amount = new BigDecimal("100");
        var type = TransactionType.DEPOSIT;

        // when
        ledgerService.recordTransaction(amount, type);

        // then
        assertThat(ledgerService.getTransactionHistory()).hasSize(1);
        assertThat(ledgerService.getTransactionHistory().get(0).getAmount()).isEqualByComparingTo(amount);
        assertThat(ledgerService.getTransactionHistory().get(0).getType()).isEqualTo(type);
    }

}
