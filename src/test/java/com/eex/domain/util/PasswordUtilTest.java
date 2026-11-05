package com.eex.domain.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void testHashPassword_andMatches() {
        String raw = "mySecretPassword";
        String hashed = PasswordUtil.hashPassword(raw);

        assertNotNull(hashed);
        assertNotEquals(raw, hashed);
        assertTrue(PasswordUtil.matches(raw, hashed));
        assertFalse(PasswordUtil.matches("wrongPassword", hashed));
    }
}

