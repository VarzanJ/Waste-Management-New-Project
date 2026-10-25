package com.ecosmart.Controller;

import com.ecosmart.model.Admin;
import com.ecosmart.repository.AdminRepository;
import com.ecosmart.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import com.ecosmart.model.WasteReport;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
public class AdminController {

    private final AdminRepository adminRepository;
    private final ReportService reportService;
        private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminController(AdminRepository adminRepository, ReportService reportService) {
        this.adminRepository = adminRepository;
        this.reportService = reportService;
    }

    @GetMapping("/admin/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("admin") != null) {
            return "redirect:/admin";
        }
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        Optional<Admin> admin = adminRepository.findByUsername(username.trim());

      if (admin.isPresent() && passwordMatches(admin.get(), password)) {
            session.setAttribute("admin", admin.get().getUsername());
            return "redirect:/admin";
        }

        model.addAttribute("error", "Invalid username or password");
        return "admin-login";
    }

    // Dashboard: stats + filtered reports
    @GetMapping("/admin")
    public String dashboard(@RequestParam(name = "location", defaultValue = "") String location,
                            @RequestParam(name = "type", defaultValue = "") String type,
                            @RequestParam(name = "status", defaultValue = "") String status,
                            HttpSession session,
                            Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("adminName", session.getAttribute("admin"));
        model.addAttribute("stats", reportService.getStats());
        model.addAttribute("category", reportService.getCategoryData());
        model.addAttribute("reports", reportService.filterReports(location, type, status));

        model.addAttribute("types", List.of("Organic", "Plastic", "Paper", "Metal", "Other"));
        model.addAttribute("statuses", List.of("Pending", "In Progress", "Collected"));

        model.addAttribute("location", location);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        return "admin-dashboard";
    }

    // Start Progress / Mark Collected
    @PostMapping("/admin/status")
    public String updateStatus(@RequestParam("id") Integer id,
                               @RequestParam("status") String status,
                               HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        reportService.updateStatus(id, status);
        return "redirect:/admin";
    }

    // Delete
    @PostMapping("/admin/delete")
    public String delete(@RequestParam("id") Integer id, HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        reportService.deleteReport(id);
        return "redirect:/admin";
    }
    // CSV download
    @GetMapping("/admin/export")
    public void exportCsv(HttpSession session, HttpServletResponse response) throws IOException {
        if (session.getAttribute("admin") == null) {
            response.sendRedirect("/admin/login");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=waste_reports.csv");

        PrintWriter writer = response.getWriter();
        writer.println("Tracking ID,Waste Type,Quantity (kg),Location,Date,Status");

        for (WasteReport r : reportService.filterReports("", "", "")) {
            writer.println(r.getTrackingId() + ","
                    + r.getWasteType() + ","
                    + r.getQuantityKg() + ",\""
                    + r.getLocation().replace("\"", "\"\"") + "\","
                    + r.getSubmittedDate() + ","
                    + r.getStatus());
        }
        writer.flush();
    }
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
        // BCrypt check. Pazhaiya SHA-256 password irundha, login aanadhum BCrypt-ku maathidum
    private boolean passwordMatches(Admin admin, String rawPassword) {
        String stored = admin.getPassword();

        if (stored.startsWith("$2")) {
            return encoder.matches(rawPassword, stored);
        }

        if (stored.equals(hash(rawPassword))) {
            admin.setPassword(encoder.encode(rawPassword));
            adminRepository.save(admin);
            return true;
        }
        return false;
    }

    private String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}