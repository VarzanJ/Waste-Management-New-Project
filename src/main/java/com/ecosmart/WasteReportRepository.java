package com.ecosmart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WasteReportRepository
        extends JpaRepository<WasteReport, Long> {

    Optional<WasteReport> findByTrackingId(String trackingId);

    boolean existsByTrackingId(String trackingId);
}