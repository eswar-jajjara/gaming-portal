package com.proarena.auth;

import com.proarena.security.JwtService;
import com.proarena.user.User;
import com.proarena.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users; this.passwords = passwords; this.jwt = jwt;
    }

    public record SignupRequest(@NotBlank @Size(min=3,max=40) String username,
                                @NotBlank @Email String email,
                                @NotBlank @Size(min=8,max=72) String password,
                                String confirmPassword) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, Player player) {}
    public record Player(Long id, String username, String email, Instant createdAt) {
        static Player from(User u) { return new Player(u.getId(),u.getUsername(),u.getEmail(),u.getCreatedAt()); }
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        if (request.confirmPassword() != null && !request.password().equals(request.confirmPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        if (users.existsByEmailIgnoreCase(request.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        if (users.existsByUsernameIgnoreCase(request.username()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already taken");
        User user = users.save(new User(request.username().trim(), request.email().trim(), passwords.encode(request.password())));
        return new AuthResponse(jwt.generate(user.getEmail()), Player.from(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .filter(found -> passwords.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthResponse(jwt.generate(user.getEmail()), Player.from(user));
    }

    @GetMapping("/me")
    public Player me(org.springframework.security.core.Authentication authentication) {
        return users.findByEmailIgnoreCase(authentication.getName()).map(Player::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}