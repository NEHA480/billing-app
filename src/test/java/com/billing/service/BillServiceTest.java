package com.billing.service;

import com.billing.entity.Bill;
import com.billing.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.isNull;

class BillServiceTest {

    @Mock private BillRepository billRepository;
    @Mock private AuditLogService auditLogService;
    @InjectMocks private BillService billService;

    private Bill bill;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bill = new Bill("John Doe", "Monthly bill", 500.0, "PENDING", LocalDate.of(2026, 5, 4));
        bill.setId(1L);
        bill.setCustomerEmail("john@gmail.com");
    }

    @Test
    void getAllBills_withPageable_returnsList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bill> page = new PageImpl<>(List.of(bill));
        when(billRepository.findAll(pageable)).thenReturn(page);
        Page<Bill> result = billService.getAllBills(pageable);
        assertEquals(1, result.getTotalElements());
        verify(billRepository).findAll(pageable);
    }

    @Test
    void getAllBills_withoutPageable_returnsList() {
        when(billRepository.findAll()).thenReturn(List.of(bill));
        List<Bill> result = billService.getAllBills();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getCustomerName());
    }

    @Test
    void searchBills_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bill> page = new PageImpl<>(List.of(bill));
        when(billRepository.searchBills(any(), eq("John"), eq("PENDING"), isNull(), isNull(), eq(pageable))).thenReturn(page);
        Page<Bill> result = billService.searchBills("John", "PENDING", null, null, pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getBillById_found_returnsBill() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        Bill result = billService.getBillById(1L);
        assertEquals("John Doe", result.getCustomerName());
    }

    @Test
    void getBillById_notFound_throwsException() {
        when(billRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> billService.getBillById(99L));
    }

    @Test
    void createBill_savesAndLogsAudit() {
        when(billRepository.save(any(Bill.class))).thenReturn(bill);
        Bill result = billService.createBill(bill);
        assertEquals("John Doe", result.getCustomerName());
        verify(billRepository).save(bill);
        verify(auditLogService).log(eq("CREATE_BILL"), any(), any());
    }

    @Test
    void updateBill_updatesFieldsAndLogsAudit() {
        Bill updated = new Bill("Jane Doe", "Updated", 700.0, "PAID", LocalDate.of(2026, 5, 5));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);
        Bill result = billService.updateBill(1L, updated);
        assertEquals("Jane Doe", result.getCustomerName());
        verify(auditLogService).log(eq("UPDATE_BILL"), any(), any());
    }

    @Test
    void updateStatus_changesStatusAndLogsAudit() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);
        Bill result = billService.updateStatus(1L, "PAID");
        assertEquals("PAID", result.getStatus());
        verify(auditLogService).log(eq("UPDATE_STATUS"), any(), any());
    }

    @Test
    void partialUpdateBill_updatesFieldsAndLogsAudit() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);
        Map<String, Object> fields = Map.of("status", "PAID", "amount", "600.0");
        Bill result = billService.partialUpdateBill(1L, fields);
        assertEquals("PAID", result.getStatus());
        verify(auditLogService).log(eq("PATCH_BILL"), any(), any());
    }

    @Test
    void deleteBill_deletesAndLogsAudit() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        doNothing().when(billRepository).deleteById(1L);
        assertDoesNotThrow(() -> billService.deleteBill(1L));
        verify(billRepository).deleteById(1L);
        verify(auditLogService).log(eq("DELETE_BILL"), any(), any());
    }

    @Test
    void deleteBill_notFound_throwsException() {
        when(billRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> billService.deleteBill(99L));
    }
}
