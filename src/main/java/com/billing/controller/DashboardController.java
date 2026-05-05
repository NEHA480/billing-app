package com.billing.controller;

import com.billing.dto.DashboardStats;
import com.billing.entity.User;
import com.billing.repository.BillRepository;
import com.billing.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final BillRepository billRepository;
    private final UserRepository userRepository;

    public DashboardController(BillRepository billRepository, UserRepository userRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }

    private Long getCurrentCompanyId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getCompanyId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        Long companyId = getCurrentCompanyId();
        long total = billRepository.findByCompanyId(companyId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        double totalAmount = billRepository.sumTotalAmountByCompany(companyId);
        long pending = billRepository.countByCompanyIdAndStatus(companyId, "PENDING");
        long paid = billRepository.countByCompanyIdAndStatus(companyId, "PAID");
        long cancelled = billRepository.countByCompanyIdAndStatus(companyId, "CANCELLED");
        double pendingAmount = billRepository.sumAmountByCompanyAndStatus(companyId, "PENDING");
        double paidAmount = billRepository.sumAmountByCompanyAndStatus(companyId, "PAID");
        return ResponseEntity.ok(new DashboardStats(total, totalAmount, pending, paid, cancelled, pendingAmount, paidAmount));
    }
}
