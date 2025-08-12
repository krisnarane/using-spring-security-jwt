package br.com.julia.using_spring_security_jwt.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.julia.using_spring_security_jwt.model.User;
import br.com.julia.using_spring_security_jwt.repository.UserRepository;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedUsers(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByUsername("admin")) {
                User u = new User();
                u.setName("Admin");
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin"));
                u.setRoles(List.of("ROLE_ADMIN"));
                repo.save(u);
            }
        };
    }
}
