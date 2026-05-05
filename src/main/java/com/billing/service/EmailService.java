package com.billing.service;

import com.billing.entity.Bill;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBillEmail(Bill bill) {
        if (bill.getCustomerEmail() == null || bill.getCustomerEmail().isEmpty()) {
            throw new RuntimeException("Customer email is missing.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(bill.getCustomerEmail());
        message.setSubject("Your Bill #" + bill.getId() + " - Billing App");
        message.setText(
            "Dear " + bill.getCustomerName() + ",\n\n" +
            "Your bill details:\n" +
            "Bill ID     : " + bill.getId() + "\n" +
            "Description : " + bill.getDescription() + "\n" +
            "Amount      : Rs." + bill.getAmount() + "\n" +
            "Status      : " + bill.getStatus() + "\n" +
            "Date        : " + bill.getBillDate() + "\n\n" +
            "Thank you for your business!\n" +
            "Billing App Team"
        );
        mailSender.send(message);
    }

    public void sendPaymentReminder(Bill bill) {
        if (bill.getCustomerEmail() == null || bill.getCustomerEmail().isEmpty()) {
            throw new RuntimeException("Customer email is missing. Cannot send reminder.");
        }
        if (!"PENDING".equals(bill.getStatus())) {
            throw new RuntimeException("Reminder can only be sent for PENDING bills.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(bill.getCustomerEmail());
        message.setSubject("Payment Reminder - Bill #" + bill.getId());
        message.setText(
            "Dear " + bill.getCustomerName() + ",\n\n" +
            "This is a reminder that your bill of Rs." + bill.getAmount() + " is still PENDING.\n\n" +
            "Bill ID     : " + bill.getId() + "\n" +
            "Description : " + bill.getDescription() + "\n" +
            "Due Date    : " + bill.getBillDate() + "\n\n" +
            "Please make the payment at your earliest convenience.\n\n" +
            "Billing App Team"
        );
        mailSender.send(message);
    }
}
