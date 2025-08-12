package br.com.julia.using_spring_security_jwt.controller;

import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.julia.using_spring_security_jwt.dtos.LoginRequest;
import br.com.julia.using_spring_security_jwt.dtos.TokenResponse;
import br.com.julia.using_spring_security_jwt.model.User;
import br.com.julia.using_spring_security_jwt.repository.UserRepository;
import br.com.julia.using_spring_security_jwt.security.JWTCreator;
import br.com.julia.using_spring_security_jwt.security.JWTObject;
import br.com.julia.using_spring_security_jwt.security.SecurityConfig;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityConfig securityConfig;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, SecurityConfig securityConfig, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.securityConfig = securityConfig;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            authenticationManager.authenticate(authToken);

            User user = userRepository.findByUsername(request.getUsername());

            JWTObject jwtObject = new JWTObject();
            jwtObject.setSubject(user.getUsername());
            jwtObject.setIssuedAt(new Date());
            jwtObject.setExpiration(new Date(System.currentTimeMillis() + securityConfig.getExpiration()));
            jwtObject.setRoles(user.getRoles());

            String token = JWTCreator.create(securityConfig.getPrefix(), securityConfig.getKey(), jwtObject);
            return ResponseEntity.ok(new TokenResponse("Login realizado com sucesso!", token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new TokenResponse("Credenciais inválidas, tente novamente."));
        }
    }
}
