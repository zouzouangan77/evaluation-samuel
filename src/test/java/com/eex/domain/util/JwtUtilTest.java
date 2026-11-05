package com.eex.domain.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "AZERTYUIOP0123456789AZERTYUIOP0123456789AZERTYUIOP0123456789");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 3600000L);
    }

    @Test
    void testGenerateToken_andValidateSuccess() {
        Integer userId = 42;
        String token = jwtUtil.generateToken(userId);

        assertNotNull(token);
        Integer extractedUserId = jwtUtil.validateAndGetUserId(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testValidateInvalidToken() {
        assertNull(jwtUtil.validateAndGetUserId("invalid.token.structure"));
        assertNull(jwtUtil.validateAndGetUserId(null));
    }
}

