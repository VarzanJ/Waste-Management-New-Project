package com.ecosmart;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "waste_reports")
public class WasteReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String wasteType;

    @Column(nullable = false)
    private double quantityKg;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false, length = 20)
    private String status = "Pending";

    @Column(unique = true, nullable = false, length = 36)
    private String trackingId;

    public WasteReport() {
    }

    @PrePersist
    public void setDefaultValues() {
        if (reportDate == null) {
            reportDate = LocalDate.now();
        }

        if (status == null || status.isBlank()) {
            status = "Pending";
        }

        if (trackingId == null || trackingId.isBlank()) {
            trackingId = UUID.randomUUID().toString();
        }
    }

    public Long getId() {
        return id;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    public double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }
}