package com.wn.tiny.ledger.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.Optional;

public interface TransactionPersistence {

    void addTransaction(Transaction transaction, Predicate<BigDecimal> balanceValidation);

    BigDecimal getBalance();

    Optional<Transaction> findTransaction(String id);

    List<Transaction> getTransactionHistory();

}
