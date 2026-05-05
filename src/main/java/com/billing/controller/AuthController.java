package com.billing.controller;

import com.billing.dto.AuthRequest;
import com.billing.entity.Company;
import com.billing.entity.User;
import com.billing.repository.CompanyRepository;
import com.billing.repository.UserRepository;
import com.billing.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, CompanyRepository companyRepository,
                          JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    String companyName = companyRepository.findById(user.getCompanyId())
                            .map(Company::getName).orElse("Unknown");
                    return ResponseEntity.ok(Map.of(
                            "token", jwtUtil.generateToken(username),
                            "username", user.getUsername(),
                            "role", user.getRole(),
                            "companyId", user.getCompanyId(),
                            "companyName", companyName
                    ));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String role = request.getRole();
        String companyName = request.getCompanyName();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        if (companyName == null || companyName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Company name is required"));
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
        }

        // Find existing company or create new one
        Company company = companyRepository.findByName(companyName)
                .orElseGet(() -> companyRepository.save(new Company(companyName, "")));

        User user = new User(username, passwordEncoder.encode(password), role, company.getId());
        userRepository.save(user);

        String token = jwtUtil.generateToken(username);
        return ResponseEntity.status(201).body(Map.of(
                "token", token,
                "username", username,
                "role", role,
                "companyId", company.getId(),
                "companyName", company.getName(),
                "message", "User registered successfully"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .map(user -> {
                    String companyName = companyRepository.findById(user.getCompanyId())
                            .map(Company::getName).orElse("Unknown");
                    return ResponseEntity.ok(Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "role", user.getRole(),
                            "companyId", user.getCompanyId(),
                            "companyName", companyName
                    ));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "User not found")));
    }
}
