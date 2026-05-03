package com.billing.controller;

import com.billing.entity.Bill;
import com.billing.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "Billing API", description = "CRUD operations for Billing Management")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    // GET all bills
    @Operation(summary = "Get all bills")
    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        return ResponseEntity.ok(billService.getAllBills());
    }

    // GET bill by ID
    @Operation(summary = "Get bill by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    // POST - create new bill
    @Operation(summary = "Create a new bill")
    @PostMapping
    public ResponseEntity<Bill> createBill(@Valid @RequestBody Bill bill) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.createBill(bill));
    }

    // PUT - full update
    @Operation(summary = "Full update of a bill")
    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(@PathVariable Long id, @Valid @RequestBody Bill bill) {
        return ResponseEntity.ok(billService.updateBill(id, bill));
    }

    // PATCH - partial update
    @Operation(summary = "Partial update of a bill")
    @PatchMapping("/{id}")
    public ResponseEntity<Bill> partialUpdateBill(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(billService.partialUpdateBill(id, fields));
    }

    // DELETE
    @Operation(summary = "Delete a bill by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok("Bill with id " + id + " deleted successfully.");
    }
}
