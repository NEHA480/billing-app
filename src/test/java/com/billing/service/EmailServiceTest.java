package com.billing.service;

import com.billing.entity.Bill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private EmailService emailService;

    private Bill bill;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bill = new Bill("John Doe", "Monthly bill", 500.0, "PENDING", LocalDate.of(2026, 5, 4));
        bill.setId(1L);
        bill.setCustomerEmail("john@gmail.com");
    }

    @Test
    void sendBillEmail_success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendBillEmail(bill));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendBillEmail_noEmail_throwsException() {
        bill.setCustomerEmail(null);
        assertThrows(RuntimeException.class, () -> emailService.sendBillEmail(bill));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendBillEmail_emptyEmail_throwsException() {
        bill.setCustomerEmail("");
        assertThrows(RuntimeException.class, () -> emailService.sendBillEmail(bill));
    }

    @Test
    void sendPaymentReminder_pendingBill_success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendPaymentReminder(bill));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPaymentReminder_paidBill_throwsException() {
        bill.setStatus("PAID");
        assertThrows(RuntimeException.class, () -> emailService.sendPaymentReminder(bill));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPaymentReminder_noEmail_throwsException() {
        bill.setCustomerEmail(null);
        assertThrows(RuntimeException.class, () -> emailService.sendPaymentReminder(bill));
    }
}
