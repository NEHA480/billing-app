package com.billing;

import com.billing.entity.Company;
import com.billing.entity.User;
import com.billing.repository.CompanyRepository;
import com.billing.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            Company company = companyRepository.findByName("Default Company")
                    .orElseGet(() -> companyRepository.save(new Company("Default Company", "admin@billing.com")));
            userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "ADMIN", company.getId()));
        }
    }
}
