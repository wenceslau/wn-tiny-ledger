package com.wn.tiny.ledger.infrastructure.repository;

import com.wn.tiny.ledger.domain.Transaction;
import com.wn.tiny.ledger.domain.InvalidTransactionException;
import com.wn.tiny.ledger.domain.TransactionPersistence;
import com.wn.tiny.ledger.domain.TransactionType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class AccountRepository implements TransactionPersistence {

    /* A thread-safe objects to store the transactions and balance in memory */
    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();
    private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);

    @Override
    public void addTransaction(Transaction transaction, Predicate<BigDecimal> balanceValidation) {
        balance.updateAndGet(currentBalance -> {

            if (!balanceValidation.test(currentBalance)) {
                throw new InvalidTransactionException("Insufficient funds for withdrawal");
            }

            return transaction.getType() == TransactionType.DEPOSIT
                    ? currentBalance.add(transaction.getAmount())
                    : currentBalance.subtract(transaction.getAmount());
        });

        transactions.add(transaction);
    }

    @Override
    public BigDecimal getBalance() {
        return balance.get();
    }

    @Override
    public Optional<Transaction> findTransaction(String id) {
        return transactions.stream()
                .filter(transaction -> transaction.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Transaction> getTransactionHistory() {
        return List.copyOf(transactions);
    }
}
