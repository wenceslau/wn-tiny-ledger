package com.wn.tiny.ledger.application;

import com.wn.tiny.ledger.domain.Transaction;
import com.wn.tiny.ledger.domain.TransactionPersistence;
import com.wn.tiny.ledger.domain.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.function.Predicate;
import java.util.List;

@Service
public class LedgerService {

    private final TransactionPersistence transactionPersistence;

    public LedgerService(TransactionPersistence transactionPersistence) {
        this.transactionPersistence = transactionPersistence;
    }

    public BigDecimal getBalance() {
        return transactionPersistence.getBalance();
    }

    public List<Transaction> getTransactionHistory() {
        return transactionPersistence.getTransactionHistory();
    }

    public Transaction recordTransaction(BigDecimal amount, TransactionType type) {

        final var transaction = new Transaction(amount, type);

        // used to validate the balance on persistence moment where I have thread safe control
        Predicate<BigDecimal> validationBalance = (currentBalance) -> {
            if (type == TransactionType.WITHDRAWAL) {
                return currentBalance.compareTo(amount) >= 0;
            }
            return true;
        };

        transactionPersistence.addTransaction(transaction, validationBalance);

        return transaction;
    }
}
