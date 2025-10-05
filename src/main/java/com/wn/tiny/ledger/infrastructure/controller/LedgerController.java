package com.wn.tiny.ledger.infrastructure.controller;

import com.wn.tiny.ledger.application.LedgerService;
import com.wn.tiny.ledger.infrastructure.controller.dto.TransactionRequest;
import com.wn.tiny.ledger.domain.Transaction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance() {
        return ResponseEntity.ok(ledgerService.getBalance());
    }

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getTransactionHistory() {
        return ResponseEntity.ok(ledgerService.getTransactionHistory());
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction createTransaction(@Valid @RequestBody TransactionRequest request) {
        return ledgerService.recordTransaction(request.amount(), request.type());
    }
}
