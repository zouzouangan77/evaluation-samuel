package com.eex.application.controller;

import com.eex.application.dto.AuthResponse;
import com.eex.application.dto.ClientLoginRequest;
import com.eex.application.dto.LoginRequest;
import com.eex.domain.entity.Client;
import com.eex.domain.entity.User;
import com.eex.domain.service.ClientService;
import com.eex.domain.service.UserService;
import com.eex.domain.util.JwtUtil;
import com.eex.domain.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final ClientService clientService;
    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Tentative de connexion pour l'utilisateur : " + loginRequest.getUsername());

        List<User> users = userService.getUsersByUsername(loginRequest.getUsername());

        if (users.isEmpty()) {
            logger.warn("Utilisateur non trouvé : " + loginRequest.getUsername());
            AuthResponse response = new AuthResponse(null, "Identifiants invalides");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        User user = users.get(0);

        if (!PasswordUtil.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Mot de passe incorrect pour l'utilisateur : " + loginRequest.getUsername());
            AuthResponse response = new AuthResponse(null, "Identifiants invalides");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getClientId());
        logger.info("Connexion réussie pour l'utilisateur : " + loginRequest.getUsername());

        AuthResponse response = new AuthResponse(token, "Authentification réussie");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/client-login")
    public ResponseEntity<AuthResponse> clientLogin(@RequestBody ClientLoginRequest clientLoginRequest) {
        logger.info("Tentative de connexion par client : clientId=" + clientLoginRequest.getClientId() + ", username=" + clientLoginRequest.getUsername());

        if (clientLoginRequest.getClientId() == null) {
            AuthResponse response = new AuthResponse(null, "L'identifiant clientId est requis");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Optional<Client> clientOptional = clientService.getClientById(clientLoginRequest.getClientId());
        if (clientOptional.isEmpty()) {
            logger.warn("Client non trouvé : " + clientLoginRequest.getClientId());
            AuthResponse response = new AuthResponse(null, "Client non trouvé");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Si des identifiants utilisateur sont également fournis, on les valide pour ce client
        if (clientLoginRequest.getUsername() != null && clientLoginRequest.getPassword() != null) {
            List<User> users = userService.getUsersByClientId(clientLoginRequest.getClientId()).stream()
                    .filter(u -> u.getUsername().equals(clientLoginRequest.getUsername()))
                    .toList();

            if (users.isEmpty() || !PasswordUtil.matches(clientLoginRequest.getPassword(), users.get(0).getPassword())) {
                logger.warn("Identifiants utilisateur invalides pour le client : " + clientLoginRequest.getClientId());
                AuthResponse response = new AuthResponse(null, "Identifiants invalides pour ce client");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            User user = users.get(0);
            String token = jwtUtil.generateToken(user.getId(), clientLoginRequest.getClientId());
            AuthResponse response = new AuthResponse(token, "Authentification client réussie");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Connexion directe avec le clientId
        String token = jwtUtil.generateToken(null, clientLoginRequest.getClientId());
        logger.info("Connexion client réussie pour le clientId : " + clientLoginRequest.getClientId());

        AuthResponse response = new AuthResponse(token, "Authentification client réussie");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("Validation du jeton");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("En-tête Authorization manquant ou invalide");
            AuthResponse response = new AuthResponse(null, "Jeton manquant ou invalide");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        Integer userId = jwtUtil.validateAndGetUserId(token);
        Integer clientId = jwtUtil.validateAndGetClientId(token);

        if (userId == null && clientId == null) {
            logger.warn("Jeton invalide ou expiré");
            AuthResponse response = new AuthResponse(null, "Jeton invalide ou expiré");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String info = userId != null ? "utilisateur : " + userId : "client : " + clientId;
        logger.info("Jeton valide pour " + info);
        AuthResponse response = new AuthResponse(token, "Jeton valide pour " + info);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
