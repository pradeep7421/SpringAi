package com.springai.service;

import com.springai.exception.DuplicateEmailException;
import com.springai.exception.UserNotFoundException;
import com.springai.model.User;
import com.springai.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public long countUsers() {
        return userRepository.count();
    }

    public User createUser(User user) {
        user.setId(null);
        ensureEmailAvailable(user.getEmail(), null);
        return userRepository.save(user);
    }

    public User updateUser(Long id, User user) {
        User existing = getUserById(id);
        ensureEmailAvailable(user.getEmail(), id);
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    public List<User> searchUsers(String name) {
        List<User> result = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (user.getName() != null && user.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(user);
            }
        }
        return result;
    }

    // currentUserId is the user being updated (null on create), so keeping your own email is allowed.
    private void ensureEmailAvailable(String email, Long currentUserId) {
        if (email == null) {
            return;
        }
        Optional<User> owner = userRepository.findByEmail(email);
        if (owner.isPresent() && !owner.get().getId().equals(currentUserId)) {
            throw new DuplicateEmailException(email);
        }
    }
}
