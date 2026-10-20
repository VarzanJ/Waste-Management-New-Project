package com.ecosmart;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    private final WasteReportRepository repository;

    public AdminController(WasteReportRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/admin")
    public String adminDashboard(
            Model model,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String wasteType,
            @RequestParam(required = false) String status) {

        List<WasteReport> allReports = repository.findAll();

        List<WasteReport> filteredReports = allReports.stream()
                .filter(r -> location == null || location.isBlank()
                        || (r.getLocation() != null
                        && r.getLocation().toLowerCase()
                        .contains(location.trim().toLowerCase())))
                .filter(r -> wasteType == null || wasteType.isBlank()
                        || wasteType.equals(r.getWasteType()))
                .filter(r -> status == null || status.isBlank()
                        || status.equals(r.getStatus()))
                .toList();

        double totalKg = allReports.stream()
                .mapToDouble(WasteReport::getQuantityKg)
                .sum();

        long pending = countStatus(allReports, "Pending");
        long inProgress = countStatus(allReports, "In Progress");
        long collected = countStatus(allReports, "Collected");

        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("Organic", 0L);
        categoryCounts.put("Plastic", 0L);
        categoryCounts.put("Paper", 0L);
        categoryCounts.put("Metal", 0L);
        categoryCounts.put("Other", 0L);

        for (WasteReport report : allReports) {
            String type = report.getWasteType();

            if (categoryCounts.containsKey(type)) {
                categoryCounts.put(
                        type, categoryCounts.get(type) + 1
                );
            } else {
                categoryCounts.put(
                        "Other", categoryCounts.get("Other") + 1
                );
            }
        }

        model.addAttribute("reports", filteredReports);
        model.addAttribute("totalReports", allReports.size());
        model.addAttribute("totalKg", totalKg);
        model.addAttribute("pending", pending);
        model.addAttribute("inProgress", inProgress);
        model.addAttribute("collected", collected);
        model.addAttribute("categoryLabels", categoryCounts.keySet());
        model.addAttribute("categoryValues", categoryCounts.values());

        model.addAttribute(
                "filterLocation", location == null ? "" : location
        );
        model.addAttribute(
                "filterWasteType", wasteType == null ? "" : wasteType
        );
        model.addAttribute(
                "filterStatus", status == null ? "" : status
        );

        return "admin";
    }

    private long countStatus(
            List<WasteReport> reports,
            String status) {

        return reports.stream()
                .filter(r -> status.equals(r.getStatus()))
                .count();
    }

    @GetMapping("/admin/reports/export")
    public ResponseEntity<byte[]> exportReports() {

        List<WasteReport> reports = repository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append(
                "ID,Waste Type,Quantity (kg),Location,Report Date,Status\r\n"
        );

        for (WasteReport report : reports) {
            csv.append(report.getId()).append(',')
                    .append(csvCell(report.getWasteType())).append(',')
                    .append(report.getQuantityKg()).append(',')
                    .append(csvCell(report.getLocation())).append(',')
                    .append(csvCell(
                            report.getReportDate() == null
                                    ? ""
                                    : report.getReportDate().toString()
                    )).append(',')
                    .append(csvCell(report.getStatus()))
                    .append("\r\n");
        }

        byte[] bytes = ("\uFEFF" + csv)
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ecosmart-waste-reports.csv\""
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private String csvCell(String value) {
        if (value == null) {
            return "\"\"";
        }

        String safe = value.replace("\"", "\"\"");

        // Reduce spreadsheet formula injection risk.
        if (safe.matches("^[\\s]*[=+@-].*")) {
            safe = "'" + safe;
        }

        return "\"" + safe + "\"";
    }
}