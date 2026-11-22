package com.springai.service;

import com.springai.exception.UserNotFoundException;
import com.springai.model.User;
import com.springai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final User alice = new User(1L, "Alice Smith", "alice@example.com", "ADMIN");
    private final User bob = new User(2L, "Bob Johnson", "bob@example.com", "USER");

    @Test
    void getAllUsersReturnsRepositoryResult() {
        List<User> users = Arrays.asList(alice, bob);
        when(userRepository.findAll()).thenReturn(users);

        assertEquals(users, userService.getAllUsers());
    }

    @Test
    void getUserByIdReturnsUserWhenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        assertSame(alice, userService.getUserById(1L));
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
        assertEquals("User not found with id: 99", ex.getMessage());
    }

    @Test
    void countUsersReturnsRepositoryCount() {
        when(userRepository.count()).thenReturn(3L);

        assertEquals(3L, userService.countUsers());
    }

    @Test
    void createUserClearsIdBeforeSaving() {
        User input = new User(42L, "David", "david@example.com", "USER");
        when(userRepository.save(input)).thenReturn(input);

        User result = userService.createUser(input);

        assertNull(input.getId());
        assertSame(input, result);
    }

    @Test
    void updateUserCopiesFieldsToExistingUser() {
        User existing = new User(1L, "Old", "old@example.com", "USER");
        User changes = new User(null, "New", "new@example.com", "ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.updateUser(1L, changes);

        assertEquals(1L, result.getId());
        assertEquals("New", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void updateUserThrowsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, new User()));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserRemovesWhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserThrowsWhenMissing() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(99L);
    }

    @Test
    void searchUsersMatchesPartialNameIgnoringCase() {
        User charlie = new User(3L, "Charlie Brown", "charlie@example.com", "USER");
        when(userRepository.findAll()).thenReturn(Arrays.asList(alice, bob, charlie));

        assertEquals(Collections.singletonList(alice), userService.searchUsers("SMITH"));
        assertEquals(Arrays.asList(bob, charlie), userService.searchUsers("o"));
    }

    @Test
    void searchUsersSkipsUsersWithNullName() {
        User noName = new User(4L, null, "x@example.com", "USER");
        when(userRepository.findAll()).thenReturn(Arrays.asList(noName, alice));

        assertEquals(Collections.singletonList(alice), userService.searchUsers("alice"));
    }

    @Test
    void searchUsersReturnsEmptyListWhenNoMatch() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(alice, bob));

        assertTrue(userService.searchUsers("xyz").isEmpty());
    }
}
