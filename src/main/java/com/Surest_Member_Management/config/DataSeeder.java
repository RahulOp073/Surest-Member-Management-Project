package com.Surest_Member_Management.config;

import com.Surest_Member_Management.entity.User;
import com.Surest_Member_Management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("admin", passwordEncoder.encode("Admin123"), "ADMIN"));
                userRepository.save(new User("user", passwordEncoder.encode("User123"), "USER"));
                System.out.println("🌱 Users seeded!");
            }
        };
    }
}
