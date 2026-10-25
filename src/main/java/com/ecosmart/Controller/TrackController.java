package com.ecosmart.Controller;

import com.ecosmart.model.WasteReport;
import com.ecosmart.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class TrackController {

    private final ReportService reportService;

    public TrackController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/track")
    public String track(@RequestParam(name = "id", required = false) String id, Model model) {
        if (id != null && !id.isBlank()) {
            model.addAttribute("searchId", id);
            Optional<WasteReport> report = reportService.findByTrackingId(id);
            if (report.isPresent()) {
                model.addAttribute("report", report.get());
            } else {
                model.addAttribute("notFound", true);
            }
        }
        return "track";
    }
}