package com.wn.tiny.ledger.infrastructure.controller;

import com.wn.tiny.ledger.application.LedgerService;
import com.wn.tiny.ledger.infrastructure.controller.dto.BalanceResponse;
import com.wn.tiny.ledger.infrastructure.controller.dto.TransactionRequest;
import com.wn.tiny.ledger.domain.Transaction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/balance")
    @Operation(summary = "Get the current balance of the ledger")
    @ApiResponse(responseCode = "200", description = "The current balance of the ledger")
    public ResponseEntity<BalanceResponse> getBalance() {
        return ResponseEntity
                .ok(new BalanceResponse(ledgerService.getBalance()));
    }

    @GetMapping("/history")
    @Operation(summary = "Get the transaction history of the ledger")
    @ApiResponse(responseCode = "200", description = "The transaction history of the ledger")
    public ResponseEntity<List<Transaction>> getTransactionHistory() {
        return ResponseEntity
                .ok(ledgerService.getTransactionHistory());
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new transaction")
    @ApiResponse(responseCode = "201", description = "The transaction was created successfully")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
        var transaction = ledgerService.recordTransaction(request.amount(), request.type());

        return ResponseEntity
                .status(HttpStatus.CREATED).body(transaction);
    }
}
