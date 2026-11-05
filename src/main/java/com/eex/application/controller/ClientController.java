package com.eex.application.controller;

import com.eex.domain.entity.Client;
import com.eex.domain.entity.User;
import com.eex.domain.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients(@AuthenticationPrincipal User currentUser) {
        // L'utilisateur ne voit que son propre client
        Optional<Client> client = clientService.getClientById(currentUser.getClientId());
        return client.map(c -> new ResponseEntity<>(List.of(c), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(List.of(), HttpStatus.OK));
    }

    @GetMapping("/me")
    public ResponseEntity<Client> getMyClient(@AuthenticationPrincipal User currentUser) {
        Optional<Client> client = clientService.getClientById(currentUser.getClientId());
        return client.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Integer id, @AuthenticationPrincipal User currentUser) {
        if (!Objects.equals(id, currentUser.getClientId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Client> client = clientService.getClientById(id);
        return client.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Integer id, @RequestBody Client clientDetails, @AuthenticationPrincipal User currentUser) {
        if (!Objects.equals(id, currentUser.getClientId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            Client updatedClient = clientService.updateClient(id, clientDetails);
            return new ResponseEntity<>(updatedClient, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
