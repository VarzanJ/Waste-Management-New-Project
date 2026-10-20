package com.ecosmart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class WasteController {

    private final WasteReportRepository repository;

    private static final Set<String> VALID_TYPES = Set.of(
            "Organic", "Plastic", "Paper", "Metal", "Other"
    );

    private static final Set<String> VALID_STATUSES = Set.of(
            "Pending", "In Progress", "Collected"
    );

    public WasteController(WasteReportRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<WasteReport> reports = repository.findAll();

        addDashboardData(model, reports);

        model.addAttribute("reports", reports);
        return "index";
    }

    private void addDashboardData(
            Model model,
            List<WasteReport> reports) {

        double totalKg = reports.stream()
                .mapToDouble(WasteReport::getQuantityKg)
                .sum();

        long pending = countStatus(reports, "Pending");
        long inProgress = countStatus(reports, "In Progress");
        long collected = countStatus(reports, "Collected");

        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("Organic", 0L);
        categoryCounts.put("Plastic", 0L);
        categoryCounts.put("Paper", 0L);
        categoryCounts.put("Metal", 0L);
        categoryCounts.put("Other", 0L);

        for (WasteReport report : reports) {
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

        model.addAttribute("totalReports", reports.size());
        model.addAttribute("totalKg", totalKg);
        model.addAttribute("pending", pending);
        model.addAttribute("inProgress", inProgress);
        model.addAttribute("collected", collected);
        model.addAttribute("categoryLabels", categoryCounts.keySet());
        model.addAttribute("categoryValues", categoryCounts.values());
    }

    private long countStatus(
            List<WasteReport> reports,
            String status) {

        return reports.stream()
                .filter(r -> status.equals(r.getStatus()))
                .count();
    }

    @PostMapping("/reports")
    public String addReport(
            @RequestParam(required = false) String wasteType,
            @RequestParam(required = false) String quantityKg,
            @RequestParam(required = false) String location,
            RedirectAttributes redirectAttributes) {

        if (wasteType == null || !VALID_TYPES.contains(wasteType)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please select a valid waste category."
            );
            return "redirect:/";
        }

        double quantity;

        try {
            quantity = Double.parseDouble(
                    quantityKg == null ? "" : quantityKg.trim()
            );
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute(
                    "error", "Enter a valid waste quantity."
            );
            return "redirect:/";
        }

        if (!Double.isFinite(quantity)
                || quantity <= 0
                || quantity > 1_000_000) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Quantity must be greater than 0 and no more than 1,000,000 kg."
            );
            return "redirect:/";
        }

        if (location == null
                || location.isBlank()
                || location.trim().length() > 150) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Enter a location between 1 and 150 characters."
            );
            return "redirect:/";
        }

        WasteReport report = new WasteReport();
        report.setWasteType(wasteType);
        report.setQuantityKg(quantity);
        report.setLocation(location.trim());
        report.setStatus("Pending");

        repository.save(report);

        redirectAttributes.addFlashAttribute(
                "success",
                "Report submitted successfully! Tracking ID: "
                        + report.getTrackingId()
        );

        return "redirect:/";
    }

    @PostMapping("/reports/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {

        if (status == null || !VALID_STATUSES.contains(status)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Invalid collection status."
            );
            return "redirect:/admin";
        }

        WasteReport report = repository.findById(id).orElse(null);

        if (report == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Report not found."
            );
            return "redirect:/admin";
        }

        report.setStatus(status);
        repository.save(report);

        redirectAttributes.addFlashAttribute(
                "success", "Report status updated successfully."
        );

        return "redirect:/admin";
    }

    @PostMapping("/reports/{id}/delete")
    public String deleteReport(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (!repository.existsById(id)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Report not found."
            );
            return "redirect:/admin";
        }

        repository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "success", "Report deleted successfully."
        );

        return "redirect:/admin";
    }
}