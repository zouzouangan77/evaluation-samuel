package com.eex.application.controller;

import com.eex.application.dto.AccountUpdateRequest;
import com.eex.domain.entity.Account;
import com.eex.domain.entity.User;
import com.eex.domain.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts(@AuthenticationPrincipal User currentUser) {
        List<Account> accounts = accountService.getAccountsByClientId(currentUser.getClientId());
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Integer id, @AuthenticationPrincipal User currentUser) {
        Optional<Account> account = accountService.getAccountById(id);
        if (account.isPresent() && Objects.equals(account.get().getClientId(), currentUser.getClientId())) {
            return new ResponseEntity<>(account.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Account>> getAccountsByClientId(@PathVariable Integer clientId, @AuthenticationPrincipal User currentUser) {
        if (!Objects.equals(clientId, currentUser.getClientId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<Account> accounts = accountService.getAccountsByClientId(clientId);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Account>> getAccountsByName(@RequestParam String name, @AuthenticationPrincipal User currentUser) {
        List<Account> accounts = accountService.getAccountsByName(name).stream()
                .filter(acc -> Objects.equals(acc.getClientId(), currentUser.getClientId()))
                .toList();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account, @AuthenticationPrincipal User currentUser) {
        account.setClientId(currentUser.getClientId());
        Account createdAccount = accountService.createAccount(account);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(
            @PathVariable Integer id,
            @RequestBody AccountUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser
    ) {
        Optional<Account> account = accountService.getAccountById(id);
        if (account.isEmpty() || !Objects.equals(account.get().getClientId(), currentUser.getClientId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            Account updatedAccount = accountService.updateAccountName(id, updateRequest.getName());
            return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Integer id, @AuthenticationPrincipal User currentUser) {
        Optional<Account> account = accountService.getAccountById(id);
        if (account.isEmpty() || !Objects.equals(account.get().getClientId(), currentUser.getClientId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            accountService.deleteAccount(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
