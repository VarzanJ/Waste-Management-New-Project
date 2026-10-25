package com.ecosmart.repository;

import com.ecosmart.model.WasteReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WasteReportRepository extends JpaRepository<WasteReport, Integer> {

    Optional<WasteReport> findByTrackingId(String trackingId);

    List<WasteReport> findTop5ByOrderByIdDesc();

    long countByStatus(String status);

    @Query("SELECT r FROM WasteReport r WHERE "
         + "(:location = '' OR LOWER(r.location) LIKE LOWER(CONCAT('%', :location, '%'))) "
         + "AND (:type = '' OR r.wasteType = :type) "
         + "AND (:status = '' OR r.status = :status) "
         + "ORDER BY r.id DESC")
    List<WasteReport> filter(@Param("location") String location,
                             @Param("type") String type,
                             @Param("status") String status);
}