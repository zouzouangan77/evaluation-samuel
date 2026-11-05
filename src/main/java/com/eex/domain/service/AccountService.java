package com.eex.domain.service;

import com.eex.domain.entity.Account;
import com.eex.infrastructure.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(Integer id) {
        return accountRepository.findById(id);
    }

    public List<Account> getAccountsByClientId(Integer clientId) {
        return accountRepository.findByClientId(clientId);
    }

    public List<Account> getAccountsByName(String name) {
        return accountRepository.findByName(name);
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account updateAccount(Integer id, Account accountDetails) {
        return updateAccountName(id, accountDetails != null ? accountDetails.getName() : null);
    }

    public Account updateAccountName(Integer id, String newName) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            if (newName != null) {
                account.setName(newName);
            }
            // Strictement le nom uniquement : le montant (amount) et clientId ne sont jamais modifiés
            return accountRepository.save(account);
        }
        throw new RuntimeException("Compte introuvable avec l'id " + id);
    }

    public void deleteAccount(Integer id) {
        if (accountRepository.existsById(id)) {
            accountRepository.deleteById(id);
        } else {
            throw new RuntimeException("Compte introuvable avec l'id " + id);
        }
    }
}
