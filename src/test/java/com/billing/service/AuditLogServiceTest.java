package com.billing.service;

import com.billing.entity.AuditLog;
import com.billing.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLogServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @InjectMocks private AuditLogService auditLogService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void log_savesAuditLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());
        assertDoesNotThrow(() -> auditLogService.log("CREATE_BILL", "admin", "Created bill #1"));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getLogs_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        AuditLog log = new AuditLog("CREATE_BILL", "admin", "Created bill #1");
        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findAllByOrderByTimestampDesc(pageable)).thenReturn(page);
        Page<AuditLog> result = auditLogService.getLogs(pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("CREATE_BILL", result.getContent().get(0).getAction());
    }
}
