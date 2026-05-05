package com.billing.controller;

import com.billing.entity.Bill;
import com.billing.security.JwtFilter;
import com.billing.security.JwtUtil;
import com.billing.service.BillService;
import com.billing.service.EmailService;
import com.billing.service.PdfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = BillController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
)
class BillControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WebApplicationContext context;

    @MockBean private BillService billService;
    @MockBean private EmailService emailService;
    @MockBean private PdfService pdfService;
    @MockBean private JwtUtil jwtUtil;

    private Bill bill;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(get("/").with(csrf()))
                .build();
        bill = new Bill("John Doe", "Monthly bill", 500.0, "PENDING", LocalDate.of(2026, 5, 4));
        bill.setId(1L);
        bill.setCustomerEmail("john@gmail.com");
    }

    @Test
    @WithMockUser
    void getAllBills_returns200() throws Exception {
        Page<Bill> page = new PageImpl<>(List.of(bill), PageRequest.of(0, 10), 1);
        when(billService.getAllBills(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/bills").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void searchBills_returns200() throws Exception {
        Page<Bill> page = new PageImpl<>(List.of(bill), PageRequest.of(0, 10), 1);
        when(billService.searchBills(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/bills/search").with(csrf()).param("customerName", "John"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getBillById_returns200() throws Exception {
        when(billService.getBillById(1L)).thenReturn(bill);
        mockMvc.perform(get("/api/bills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    @Test
    @WithMockUser
    void createBill_returns201() throws Exception {
        when(billService.createBill(any(Bill.class))).thenReturn(bill);
        mockMvc.perform(post("/api/bills").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bill)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    @Test
    @WithMockUser
    void updateBill_returns200() throws Exception {
        when(billService.updateBill(eq(1L), any(Bill.class))).thenReturn(bill);
        mockMvc.perform(put("/api/bills/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bill)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    @Test
    @WithMockUser
    void updateStatus_returns200() throws Exception {
        bill.setStatus("PAID");
        when(billService.updateStatus(eq(1L), eq("PAID"))).thenReturn(bill);
        mockMvc.perform(patch("/api/bills/1/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @WithMockUser
    void deleteBill_returns200() throws Exception {
        doNothing().when(billService).deleteBill(1L);
        mockMvc.perform(delete("/api/bills/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Bill with id 1 deleted successfully."));
    }

    @Test
    @WithMockUser
    void sendReminder_returns200() throws Exception {
        when(billService.getBillById(1L)).thenReturn(bill);
        doNothing().when(emailService).sendPaymentReminder(any(Bill.class));
        mockMvc.perform(post("/api/bills/1/remind").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Reminder sent to john@gmail.com"));
    }

    @Test
    @WithMockUser
    void sendReminder_emailFails_returns500() throws Exception {
        when(billService.getBillById(1L)).thenReturn(bill);
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendPaymentReminder(any(Bill.class));
        mockMvc.perform(post("/api/bills/1/remind").with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("Failed to send reminder")));
    }

    @Test
    @WithMockUser
    void exportBillPdf_returns200() throws Exception {
        when(billService.getBillById(1L)).thenReturn(bill);
        when(pdfService.generateBillPdf(any(Bill.class))).thenReturn(new byte[]{1, 2, 3});
        mockMvc.perform(get("/api/bills/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=bill-1.pdf"));
    }

    @Test
    @WithMockUser
    void exportAllBillsPdf_returns200() throws Exception {
        when(billService.getAllBills()).thenReturn(List.of(bill));
        when(pdfService.generateAllBillsPdf(anyList())).thenReturn(new byte[]{1, 2, 3});
        mockMvc.perform(get("/api/bills/pdf/all"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=all-bills.pdf"));
    }
}
