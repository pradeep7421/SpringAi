package com.springai.repository;

import com.springai.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// In-memory dummy repository; data resets on application restart.
@Repository
public class UserRepository {

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public UserRepository() {
        save(new User(null, "Alice Smith", "alice@example.com", "ADMIN"));
        save(new User(null, "Bob Johnson", "bob@example.com", "USER"));
        save(new User(null, "Charlie Brown", "charlie@example.com", "USER"));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        for (User user : store.values()) {
            if (email.equalsIgnoreCase(user.getEmail())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.incrementAndGet());
        }
        store.put(user.getId(), user);
        return user;
    }

    public long count() {
        return store.size();
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
