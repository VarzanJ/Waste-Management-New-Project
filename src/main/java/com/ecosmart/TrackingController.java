
package com.ecosmart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TrackingController {

    private final WasteReportRepository wasteReportRepository;

    public TrackingController(
            WasteReportRepository wasteReportRepository) {
        this.wasteReportRepository = wasteReportRepository;
    }

    @GetMapping("/track")
    public String showTrackingPage() {
        return "track";
    }

    @GetMapping("/track/search")
    public String trackReport(
            @RequestParam(name = "trackingId", defaultValue = "")
            String trackingId,
            Model model) {

        String searchId = trackingId.trim();
        model.addAttribute("trackingId", searchId);

        if (searchId.isBlank()) {
            model.addAttribute("error",
                    "Please enter your Tracking ID.");
            return "track";
        }

        wasteReportRepository.findByTrackingId(searchId)
                .ifPresentOrElse(
                    report -> model.addAttribute("report", report),
                    () -> model.addAttribute("error",
                        "No report found. Please check your Tracking ID.")
                );

        return "track";
    }
}
