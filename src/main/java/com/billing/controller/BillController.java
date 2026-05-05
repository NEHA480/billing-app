package com.billing.controller;

import com.billing.entity.Bill;
import com.billing.service.BillService;
import com.billing.service.EmailService;
import com.billing.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "Billing API", description = "CRUD operations for Billing Management")
public class BillController {

    private final BillService billService;
    private final EmailService emailService;
    private final PdfService pdfService;

    public BillController(BillService billService, EmailService emailService, PdfService pdfService) {
        this.billService = billService;
        this.emailService = emailService;
        this.pdfService = pdfService;
    }

    @Operation(summary = "Get all bills with pagination")
    @GetMapping
    public ResponseEntity<Page<Bill>> getAllBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return ResponseEntity.ok(billService.getAllBills(pageable));
    }

    @Operation(summary = "Search and filter bills")
    @GetMapping("/search")
    public ResponseEntity<Page<Bill>> searchBills(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(billService.searchBills(customerName, status, from, to, pageable));
    }

    @Operation(summary = "Get bill by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    @Operation(summary = "Create a new bill")
    @PostMapping
    public ResponseEntity<Bill> createBill(@Valid @RequestBody Bill bill) {
        Bill saved = billService.createBill(bill);
        if (saved.getCustomerEmail() != null && !saved.getCustomerEmail().isEmpty()) {
            try { emailService.sendBillEmail(saved); } catch (Exception ignored) {}
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Full update of a bill")
    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(@PathVariable Long id, @Valid @RequestBody Bill bill) {
        return ResponseEntity.ok(billService.updateBill(id, bill));
    }

    @Operation(summary = "Update bill status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Bill> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(billService.updateStatus(id, body.get("status")));
    }

    @Operation(summary = "Partial update of a bill")
    @PatchMapping("/{id}")
    public ResponseEntity<Bill> partialUpdateBill(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(billService.partialUpdateBill(id, fields));
    }

    @Operation(summary = "Delete a bill by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok("Bill with id " + id + " deleted successfully.");
    }

    @Operation(summary = "Send payment reminder email")
    @PostMapping("/{id}/remind")
    public ResponseEntity<String> sendReminder(@PathVariable Long id) {
        Bill bill = billService.getBillById(id);
        try {
            emailService.sendPaymentReminder(bill);
            return ResponseEntity.ok("Reminder sent to " + bill.getCustomerEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send reminder: " + e.getMessage());
        }
    }

    @Operation(summary = "Export single bill as PDF")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportBillPdf(@PathVariable Long id) {
        try {
            Bill bill = billService.getBillById(id);
            byte[] pdf = pdfService.generateBillPdf(bill);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bill-" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Export all bills as PDF")
    @GetMapping("/pdf/all")
    public ResponseEntity<byte[]> exportAllBillsPdf() {
        try {
            List<Bill> bills = billService.getAllBills();
            byte[] pdf = pdfService.generateAllBillsPdf(bills);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all-bills.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
