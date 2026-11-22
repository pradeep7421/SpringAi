package com.springai.repository;

import com.springai.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void constructorSeedsThreeUsers() {
        assertEquals(3, repository.count());
        assertEquals("Alice Smith", repository.findById(1L).get().getName());
    }

    @Test
    void findAllReturnsAllUsers() {
        List<User> users = repository.findAll();

        assertEquals(3, users.size());
    }

    @Test
    void findByIdReturnsUserWhenPresent() {
        Optional<User> user = repository.findById(2L);

        assertTrue(user.isPresent());
        assertEquals("Bob Johnson", user.get().getName());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertFalse(repository.findById(999L).isPresent());
    }

    @Test
    void saveAssignsNextIdToNewUser() {
        User saved = repository.save(new User(null, "David", "david@example.com", "USER"));

        assertEquals(4L, saved.getId());
        assertEquals(4, repository.count());
    }

    @Test
    void saveKeepsIdOfExistingUser() {
        User updated = new User(1L, "Alice Updated", "alice@example.com", "ADMIN");

        repository.save(updated);

        assertEquals(3, repository.count());
        assertEquals("Alice Updated", repository.findById(1L).get().getName());
    }

    @Test
    void existsByIdReflectsStore() {
        assertTrue(repository.existsById(1L));
        assertFalse(repository.existsById(999L));
    }

    @Test
    void deleteByIdRemovesUser() {
        repository.deleteById(1L);

        assertFalse(repository.existsById(1L));
        assertEquals(2, repository.count());
    }
}
