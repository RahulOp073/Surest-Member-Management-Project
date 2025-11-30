package com.Surest_Member_Management.controller;

import com.Surest_Member_Management.entity.User;
import com.Surest_Member_Management.repository.UserRepository;
import com.Surest_Member_Management.Exception.BadRequestException;
import com.Surest_Member_Management.Exception.InvalidCredentialsException;
import com.Surest_Member_Management.Exception.UserAlreadyExistsException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    public String jwtSecret;

    @Value("${jwt.expiration}")
    public long expirationTime;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            String role = body.getOrDefault("role", "USER").toUpperCase();

            if (username == null || password == null) {
                throw new BadRequestException("Username and password are required");
            }

            if (userRepository.findByUsername(username).isPresent()) {
                throw new UserAlreadyExistsException(username);
            }

            String hashedPassword = passwordEncoder.encode(password);
            User user = new User(username, hashedPassword, role);
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "username", user.getUsername(),
                            "role", user.getRole(),
                            "message", "User registered successfully"
                    ));
        } catch (BadRequestException | UserAlreadyExistsException ex) {
            // Handle the known exceptions and send a bad request or conflict response
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            // Catch any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + ex.getMessage()));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");

            if (username == null || password == null) {
                throw new BadRequestException("Username and password are required");
            }

            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new InvalidCredentialsException();
            }

            Date now = new Date();
            Date expiryDate = new Date(System.currentTimeMillis() + expirationTime);

            String token = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("role", user.getRole())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .compact();

            return ResponseEntity.ok(Map.of(
                    "username", user.getUsername(),
                    "role", user.getRole(),
                    "token", token
            ));
        } catch (BadRequestException | InvalidCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + ex.getMessage()));
        }
    }
}
