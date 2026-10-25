package com.ecosmart.Controller;

import com.ecosmart.model.WasteReport;
import com.ecosmart.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class HomeController {

    private final ReportService reportService;

    public HomeController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Home page kaattum
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("stats", reportService.getStats());
        model.addAttribute("category", reportService.getCategoryData());
        model.addAttribute("reports", reportService.getRecentReports());
        model.addAttribute("newReport", new WasteReport());
        return "home";
    }

    // "Submit" button click pannina
    @PostMapping("/submit")
    public String submit(@ModelAttribute("newReport") WasteReport report,
                         RedirectAttributes redirect) {

        // Quantity: 0.1 kg - 100 kg
        if (report.getQuantityKg() == null
                || report.getQuantityKg().compareTo(new BigDecimal("0.1")) < 0
                || report.getQuantityKg().compareTo(new BigDecimal("100")) > 0) {
            redirect.addFlashAttribute("error", "Quantity must be between 0.1 kg and 100 kg.");
            return "redirect:/#submit";
        }

        // Location: letters only (spaces and . , - ' allowed), numbers not allowed
        String location = report.getLocation() == null ? "" : report.getLocation().trim();
        if (!location.matches("\\p{L}[\\p{L} .,'\\-]*")) {
            redirect.addFlashAttribute("error", "Location must contain letters only. Numbers are not allowed.");
            return "redirect:/#submit";
        }
        report.setLocation(location);

        WasteReport saved = reportService.submitReport(report);
        redirect.addFlashAttribute("message",
                "Report submitted! Your Tracking ID: " + saved.getTrackingId());
        return "redirect:/#submit";
    }
}