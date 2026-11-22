package com.springai.controller;

import com.springai.exception.UserNotFoundException;
import com.springai.model.User;
import com.springai.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String USER_JSON = "{\"name\":\"David\",\"email\":\"david@example.com\",\"role\":\"USER\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final User alice = new User(1L, "Alice Smith", "alice@example.com", "ADMIN");
    private final User bob = new User(2L, "Bob Johnson", "bob@example.com", "USER");

    @Test
    void getAllUsersReturnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(alice, bob));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice Smith"));
    }

    @Test
    void countUsersReturnsNumber() throws Exception {
        when(userService.countUsers()).thenReturn(3L);

        mockMvc.perform(get("/api/users/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void getUserByIdReturnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(alice);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getUserByIdReturns404WhenMissing() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserReturns201() throws Exception {
        User created = new User(4L, "David", "david@example.com", "USER");
        when(userService.createUser(any(User.class))).thenReturn(created);

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(USER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void updateUserReturnsUpdatedUser() throws Exception {
        User updated = new User(1L, "David", "david@example.com", "USER");
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/1").contentType(MediaType.APPLICATION_JSON).content(USER_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("David"));
    }

    @Test
    void deleteUserReturns204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUserReturns404WhenMissing() throws Exception {
        doThrow(new UserNotFoundException(99L)).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchUsersReturnsMatches() throws Exception {
        when(userService.searchUsers("alice")).thenReturn(Collections.singletonList(alice));

        mockMvc.perform(get("/api/users/search").param("name", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice Smith"));
    }

    @Test
    void searchUsersWithoutNameReturns400() throws Exception {
        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isBadRequest());
    }
}
