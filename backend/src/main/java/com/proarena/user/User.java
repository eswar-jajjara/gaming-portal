package com.proarena.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "players")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String username;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {}
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
}