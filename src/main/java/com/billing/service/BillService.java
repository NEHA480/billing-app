package com.billing.service;

import com.billing.entity.Bill;
import com.billing.entity.User;
import com.billing.repository.BillRepository;
import com.billing.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public BillService(BillRepository billRepository, AuditLogService auditLogService, UserRepository userRepository) {
        this.billRepository = billRepository;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    // Get current logged-in username
    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    // Get companyId of current logged-in user
    private Long getCurrentCompanyId() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .map(User::getCompanyId)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public Page<Bill> getAllBills(Pageable pageable) {
        return billRepository.findByCompanyId(getCurrentCompanyId(), pageable);
    }

    public List<Bill> getAllBills() {
        return billRepository.findByCompanyId(getCurrentCompanyId());
    }

    public Page<Bill> searchBills(String customerName, String status, LocalDate from, LocalDate to, Pageable pageable) {
        return billRepository.searchBills(getCurrentCompanyId(), customerName, status, from, to, pageable);
    }

    public Bill getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
        // Ensure bill belongs to current company
        if (!bill.getCompanyId().equals(getCurrentCompanyId())) {
            throw new RuntimeException("Access denied to bill: " + id);
        }
        return bill;
    }

    public Bill createBill(Bill bill) {
        bill.setCompanyId(getCurrentCompanyId());
        bill.setCreatedBy(getCurrentUsername());
        Bill saved = billRepository.save(bill);
        auditLogService.log("CREATE_BILL", getCurrentUsername(), "Created bill #" + saved.getId() + " for " + saved.getCustomerName());
        return saved;
    }

    public Bill updateBill(Long id, Bill updatedBill) {
        Bill existing = getBillById(id);
        existing.setCustomerName(updatedBill.getCustomerName());
        existing.setCustomerEmail(updatedBill.getCustomerEmail());
        existing.setDescription(updatedBill.getDescription());
        existing.setAmount(updatedBill.getAmount());
        existing.setStatus(updatedBill.getStatus());
        existing.setBillDate(updatedBill.getBillDate());
        Bill saved = billRepository.save(existing);
        auditLogService.log("UPDATE_BILL", getCurrentUsername(), "Updated bill #" + id);
        return saved;
    }

    public Bill updateStatus(Long id, String status) {
        Bill bill = getBillById(id);
        String oldStatus = bill.getStatus();
        bill.setStatus(status);
        Bill saved = billRepository.save(bill);
        auditLogService.log("UPDATE_STATUS", getCurrentUsername(), "Changed bill #" + id + " status from " + oldStatus + " to " + status);
        return saved;
    }

    public Bill partialUpdateBill(Long id, Map<String, Object> fields) {
        Bill existing = getBillById(id);
        fields.forEach((key, value) -> {
            switch (key) {
                case "customerName" -> existing.setCustomerName((String) value);
                case "customerEmail" -> existing.setCustomerEmail((String) value);
                case "description"  -> existing.setDescription((String) value);
                case "amount"       -> existing.setAmount(Double.valueOf(value.toString()));
                case "status"       -> existing.setStatus((String) value);
            }
        });
        Bill saved = billRepository.save(existing);
        auditLogService.log("PATCH_BILL", getCurrentUsername(), "Partially updated bill #" + id);
        return saved;
    }

    public void deleteBill(Long id) {
        Bill bill = getBillById(id);
        billRepository.deleteById(id);
        auditLogService.log("DELETE_BILL", getCurrentUsername(), "Deleted bill #" + id + " for " + bill.getCustomerName());
    }
}
