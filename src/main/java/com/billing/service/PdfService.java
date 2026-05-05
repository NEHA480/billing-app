package com.billing.service;

import com.billing.entity.Bill;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    public byte[] generateBillPdf(Bill bill) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);

        Paragraph title = new Paragraph("BILL INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Bill ID: " + bill.getId(), headerFont));
        document.add(new Paragraph("Customer: " + bill.getCustomerName(), normalFont));
        if (bill.getCustomerEmail() != null) {
            document.add(new Paragraph("Email: " + bill.getCustomerEmail(), normalFont));
        }
        document.add(new Paragraph("Date: " + bill.getBillDate(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        addTableHeader(table, "Description", "Amount");
        addTableRow(table, bill.getDescription(), "₹" + bill.getAmount());
        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Status: " + bill.getStatus(), headerFont));
        document.add(new Paragraph("Total Amount: ₹" + bill.getAmount(), headerFont));

        document.close();
        return out.toByteArray();
    }

    public byte[] generateAllBillsPdf(List<Bill> bills) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("ALL BILLS REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        addTableHeader(table, "ID", "Customer", "Description", "Amount", "Status");

        for (Bill bill : bills) {
            addTableRow(table,
                String.valueOf(bill.getId()),
                bill.getCustomerName(),
                bill.getDescription(),
                "₹" + bill.getAmount(),
                bill.getStatus()
            );
        }
        document.add(table);

        document.close();
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String... values) {
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}
