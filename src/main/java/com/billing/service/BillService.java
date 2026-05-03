package com.billing.service;

import com.billing.entity.Bill;
import com.billing.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    // GET all bills
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    // GET bill by ID
    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
    }

    // POST - create new bill
    public Bill createBill(Bill bill) {
        return billRepository.save(bill);
    }

    // PUT - full update
    public Bill updateBill(Long id, Bill updatedBill) {
        Bill existing = getBillById(id);
        existing.setCustomerName(updatedBill.getCustomerName());
        existing.setDescription(updatedBill.getDescription());
        existing.setAmount(updatedBill.getAmount());
        existing.setStatus(updatedBill.getStatus());
        existing.setBillDate(updatedBill.getBillDate());
        return billRepository.save(existing);
    }

    // PATCH - partial update
    public Bill partialUpdateBill(Long id, Map<String, Object> fields) {
        Bill existing = getBillById(id);
        fields.forEach((key, value) -> {
            switch (key) {
                case "customerName" -> existing.setCustomerName((String) value);
                case "description"  -> existing.setDescription((String) value);
                case "amount"       -> existing.setAmount(Double.valueOf(value.toString()));
                case "status"       -> existing.setStatus((String) value);
            }
        });
        return billRepository.save(existing);
    }

    // DELETE
    public void deleteBill(Long id) {
        getBillById(id); // validate existence
        billRepository.deleteById(id);
    }
}
