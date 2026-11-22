package com.springai.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    void noArgConstructorCreatesEmptyUser() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getRole());
    }

    @Test
    void allArgsConstructorSetsFields() {
        User user = new User(1L, "Alice", "alice@example.com", "ADMIN");

        assertEquals(1L, user.getId());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    void settersUpdateFields() {
        User user = new User();
        user.setId(2L);
        user.setName("Bob");
        user.setEmail("bob@example.com");
        user.setRole("USER");

        assertEquals(2L, user.getId());
        assertEquals("Bob", user.getName());
        assertEquals("bob@example.com", user.getEmail());
        assertEquals("USER", user.getRole());
    }
}
