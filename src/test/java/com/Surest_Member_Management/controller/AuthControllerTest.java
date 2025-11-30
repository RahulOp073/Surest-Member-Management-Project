package com.Surest_Member_Management.controller;

import com.Surest_Member_Management.entity.User;
import com.Surest_Member_Management.repository.UserRepository;
import com.Surest_Member_Management.Exception.BadRequestException;
import com.Surest_Member_Management.Exception.InvalidCredentialsException;
import com.Surest_Member_Management.Exception.UserAlreadyExistsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController.jwtSecret = "12345678901234567890123456789012";
        authController.expirationTime = 3600000;
    }

    @Test
    void testRegisterSuccess() {
        Map<String, String> body = Map.of(
                "username", "john",
                "password", "secret",
                "role", "ADMIN"
        );

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("hashedpass");

        ResponseEntity<?> response = authController.register(body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<?, ?> result = (Map<?, ?>) response.getBody();

        assertEquals("john", result.get("username"));
        assertEquals("ADMIN", result.get("role"));
        assertEquals("User registered successfully", result.get("message"));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterMissingFields() {
        Map<String, String> body = Map.of("username", "john");

        ResponseEntity<?> response = authController.register(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).get("error")
                .toString().contains("Username and password are required"));
    }

    @Test
    void testRegisterUserAlreadyExists() {
        Map<String, String> body = Map.of("username", "john", "password", "secret");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(new User()));

        ResponseEntity<?> response = authController.register(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).get("error")
                .toString().contains("already exists"));
    }

    @Test
    void testRegisterUnexpectedException() {
        Map<String, String> body = Map.of("username", "john", "password", "secret");

        when(userRepository.findByUsername("john"))
                .thenThrow(new RuntimeException("DB DOWN"));

        ResponseEntity<?> response = authController.register(body);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString()
                .contains("unexpected error"));
    }

    @Test
    void testLoginSuccess() {
        Map<String, String> body = Map.of(
                "username", "john",
                "password", "secret"
        );

        User user = new User("john", "hashed", "USER");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        ResponseEntity<?> response = authController.login(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<?, ?> map = (Map<?, ?>) response.getBody();
        assertEquals("john", map.get("username"));
        assertEquals("USER", map.get("role"));
        assertNotNull(map.get("token")); // token exists
    }

    @Test
    void testLoginMissingFields() {
        Map<String, String> body = Map.of("username", "john");

        ResponseEntity<?> response = authController.login(body);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).get("error")
                .toString().contains("Username and password are required"));
    }

    @Test
    void testLoginInvalidCredentials() {
        Map<String, String> body = Map.of("username", "john", "password", "wrong");

        User user = new User("john", "hashed", "USER");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        ResponseEntity<?> response = authController.login(body);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password.", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void testLoginUserNotFound() {
        Map<String, String> body = Map.of("username", "john", "password", "secret");

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.login(body);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password.", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void testLoginUnexpectedError() {
        Map<String, String> body = Map.of("username", "john", "password", "secret");

        when(userRepository.findByUsername("john"))
                .thenThrow(new RuntimeException("DB DOWN"));

        ResponseEntity<?> response = authController.login(body);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).get("error")
                .toString().contains("unexpected error"));
    }
}
