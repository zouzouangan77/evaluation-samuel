package com.eex.infrastructure.config;

import com.eex.domain.entity.Client;
import com.eex.domain.entity.User;
import com.eex.domain.service.ClientService;
import com.eex.domain.service.UserService;
import com.eex.domain.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ClientService clientService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        final Integer userId = jwtUtil.validateAndGetUserId(token);
        final Integer clientId = jwtUtil.validateAndGetClientId(token);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            User principalUser = null;

            if (userId != null) {
                Optional<User> userOptional = userService.getUserById(userId);
                if (userOptional.isPresent()) {
                    principalUser = userOptional.get();
                }
            } else if (clientId != null) {
                // Connexion au niveau du client directement
                Optional<Client> clientOptional = clientService.getClientById(clientId);
                if (clientOptional.isPresent()) {
                    principalUser = new User();
                    principalUser.setId(null);
                    principalUser.setUsername("client_" + clientId);
                    principalUser.setClientId(clientId);
                }
            }

            if (principalUser != null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principalUser,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
