package com.eex.application.controller;

import com.eex.domain.entity.User;
import com.eex.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@AuthenticationPrincipal User currentUser) {
        // L'utilisateur ne voit que les utilisateurs de son client
        List<User> users = userService.getUsersByClientId(currentUser.getClientId());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return new ResponseEntity<>(currentUser, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id, @AuthenticationPrincipal User currentUser) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent() && Objects.equals(user.get().getClientId(), currentUser.getClientId())) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<User>> getUsersByClientId(@PathVariable Integer clientId, @AuthenticationPrincipal User currentUser) {
        if (!Objects.equals(clientId, currentUser.getClientId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<User> users = userService.getUsersByClientId(clientId);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> getUsersByUsername(@RequestParam String username, @AuthenticationPrincipal User currentUser) {
        List<User> users = userService.getUsersByUsername(username).stream()
                .filter(u -> Objects.equals(u.getClientId(), currentUser.getClientId()))
                .toList();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user, @AuthenticationPrincipal User currentUser) {
        user.setClientId(currentUser.getClientId());
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User userDetails, @AuthenticationPrincipal User currentUser) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isEmpty() || !Objects.equals(existingUser.get().getClientId(), currentUser.getClientId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            userDetails.setClientId(currentUser.getClientId());
            User updatedUser = userService.updateUser(id, userDetails);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id, @AuthenticationPrincipal User currentUser) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isEmpty() || !Objects.equals(existingUser.get().getClientId(), currentUser.getClientId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
