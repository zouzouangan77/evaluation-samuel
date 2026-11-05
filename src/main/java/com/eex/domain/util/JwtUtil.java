package com.eex.domain.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:AZERTYUIOP0123456789AZERTYUIOP0123456789AZERTYUIOP0123456789}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public String generateToken(Integer userId) {
        return generateToken(userId, null);
    }

    public String generateToken(Integer userId, Integer clientId) {
        try {
            var builder = Jwts.builder()
                    .subject(userId != null ? userId.toString() : "CLIENT_" + clientId)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration));

            if (clientId != null) {
                builder.claim("clientId", clientId);
            }

            String token = builder.signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).compact();
            logger.info("Jeton généré avec succès pour userId: " + userId + ", clientId: " + clientId);
            return token;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du jeton : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du jeton", e);
        }
    }

    public Integer validateAndGetUserId(String token) {
        try {
            Claims payload = getClaims(token);
            if (payload == null) {
                return null;
            }
            String subject = payload.getSubject();
            if (subject != null && !subject.startsWith("CLIENT_")) {
                return Integer.parseInt(subject);
            }
            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du userId du jeton : " + e.getMessage());
            return null;
        }
    }

    public Integer validateAndGetClientId(String token) {
        try {
            Claims payload = getClaims(token);
            if (payload == null) {
                return null;
            }
            Integer clientId = payload.get("clientId", Integer.class);
            if (clientId != null) {
                return clientId;
            }
            String subject = payload.getSubject();
            if (subject != null && subject.startsWith("CLIENT_")) {
                return Integer.parseInt(subject.replace("CLIENT_", ""));
            }
            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du clientId du jeton : " + e.getMessage());
            return null;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
