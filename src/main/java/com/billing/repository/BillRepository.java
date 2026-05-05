package com.billing.repository;

import com.billing.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Page<Bill> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE b.companyId = :companyId AND " +
           "(:customerName IS NULL OR LOWER(b.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:from IS NULL OR b.billDate >= :from) AND " +
           "(:to IS NULL OR b.billDate <= :to)")
    Page<Bill> searchBills(@Param("companyId") Long companyId,
                           @Param("customerName") String customerName,
                           @Param("status") String status,
                           @Param("from") LocalDate from,
                           @Param("to") LocalDate to,
                           Pageable pageable);

    long countByCompanyIdAndStatus(Long companyId, String status);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.companyId = :companyId")
    double sumTotalAmountByCompany(@Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.companyId = :companyId AND b.status = :status")
    double sumAmountByCompanyAndStatus(@Param("companyId") Long companyId, @Param("status") String status);

    List<Bill> findByCompanyId(Long companyId);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b")
    double sumTotalAmount();

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.status = :status")
    double sumAmountByStatus(@Param("status") String status);

    List<Bill> findByStatus(String status);
}
