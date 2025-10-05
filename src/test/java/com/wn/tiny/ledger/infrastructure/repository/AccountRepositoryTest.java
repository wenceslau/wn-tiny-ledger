package com.wn.tiny.ledger.infrastructure.repository;

import com.wn.tiny.ledger.domain.InvalidTransactionException;
import com.wn.tiny.ledger.domain.Transaction;
import com.wn.tiny.ledger.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AccountRepository Tests")
class AccountRepositoryTest {

    private final Predicate<BigDecimal> alwaysTrue = (balance) -> true;
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new AccountRepository();
    }

    @Test
    @DisplayName("should have a balance of zero")
    void initialBalanceIsZero() {
        assertThat(accountRepository.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("should have an empty transaction history")
    void initialHistoryIsEmpty() {
        assertThat(accountRepository.getTransactionHistory()).isEmpty();
    }

    @Test
    @DisplayName("should not find any transaction")
    void findTransaction_whenEmpty_returnsEmpty() {
        assertThat(accountRepository.findTransaction("any-id")).isEmpty();
    }

    @Test
    @DisplayName("should correctly add a deposit")
    void addTransaction_forDeposit_updatesState() {
        // given
        Transaction deposit = new Transaction(new BigDecimal("250.75"), TransactionType.DEPOSIT);

        // when
        accountRepository.addTransaction(deposit, alwaysTrue);

        // then
        assertThat(accountRepository.getBalance()).isEqualByComparingTo("250.75");
        List<Transaction> history = accountRepository.getTransactionHistory();
        assertThat(history).hasSize(1).containsExactly(deposit);

        Optional<Transaction> found = accountRepository.findTransaction(deposit.getId().toString());
        assertThat(found).isPresent().contains(deposit);
    }

    @Test
    @DisplayName("should correctly add a successful withdrawal")
    void addTransaction_forSuccessfulWithdrawal_updatesState() {
        // Given a pre-existing balance
        Transaction deposit = new Transaction(new BigDecimal("100"), TransactionType.DEPOSIT);
        accountRepository.addTransaction(deposit, alwaysTrue);

        // When
        Transaction withdrawal = new Transaction(new BigDecimal("40"), TransactionType.WITHDRAWAL);
        Predicate<BigDecimal> sufficientFunds = (balance) -> balance.compareTo(withdrawal.getAmount()) >= 0;
        accountRepository.addTransaction(withdrawal, sufficientFunds);

        // Then
        assertThat(accountRepository.getBalance()).isEqualByComparingTo("60");
        assertThat(accountRepository.getTransactionHistory()).hasSize(2).contains(deposit, withdrawal);
    }

    @Test
    @DisplayName("should throw exception and not update state for a failed withdrawal")
    void addTransaction_forFailedWithdrawal_throwsAndRollsBack() {
        // Given a pre-existing balance
        Transaction deposit = new Transaction(new BigDecimal("50"), TransactionType.DEPOSIT);
        accountRepository.addTransaction(deposit, alwaysTrue);

        // when
        Transaction withdrawal = new Transaction(new BigDecimal("100"), TransactionType.WITHDRAWAL);
        Predicate<BigDecimal> insufficientFunds = (balance) -> balance.compareTo(withdrawal.getAmount()) >= 0;

        // then
        assertThatThrownBy(() -> accountRepository.addTransaction(withdrawal, insufficientFunds))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Insufficient funds for withdrawal");

        assertThat(accountRepository.getBalance()).isEqualByComparingTo("50");
        assertThat(accountRepository.getTransactionHistory()).hasSize(1).containsExactly(deposit);
    }

    @Test
    @DisplayName("getTransactionHistory should return an immutable list")
    void getTransactionHistory_returnsImmutableList() {
        accountRepository.addTransaction(new Transaction(BigDecimal.TEN, TransactionType.DEPOSIT), (b) -> true);
        List<Transaction> history = accountRepository.getTransactionHistory();
        assertThatThrownBy(() -> history.add(null)).isInstanceOf(UnsupportedOperationException.class);
    }
}
