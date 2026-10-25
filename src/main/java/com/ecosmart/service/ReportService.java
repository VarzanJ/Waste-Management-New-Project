package com.ecosmart.service;

import com.ecosmart.model.WasteReport;
import com.ecosmart.repository.WasteReportRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportService {

    private final WasteReportRepository repository;

    public ReportService(WasteReportRepository repository) {
        this.repository = repository;
    }

    // New report save panradhu (tracking ID automatic)
    public WasteReport submitReport(WasteReport report) {
        report.setTrackingId(generateTrackingId());
        report.setStatus("Pending");
        report.setSubmittedDate(LocalDate.now());
        return repository.save(report);
    }

    // ES-1043 maathiri unique ID generate panradhu
    private String generateTrackingId() {
        Random random = new Random();
        String id;
        do {
            id = "ES-" + (1000 + random.nextInt(9000));
        } while (repository.findByTrackingId(id).isPresent());
        return id;
    }

    // Track Report page-ku
    // "ID: ES-7369", "#ES-7369", "es-7369" ellam work aagum
    public Optional<WasteReport> findByTrackingId(String input) {
        String text = input == null ? "" : input.trim().toUpperCase();

        Matcher matcher = Pattern.compile("ES-\\d+").matcher(text);
        String id = matcher.find() ? matcher.group() : text.replace("#", "");

        return repository.findByTrackingId(id);
    }

    // Home page "Recent Reports"-ku
    public List<WasteReport> getRecentReports() {
        return repository.findTop5ByOrderByIdDesc();
    }

    // Home page stats cards-ku
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalKg = repository.findAll().stream()
                .map(WasteReport::getQuantityKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalReports", repository.count());
        stats.put("totalKg", totalKg);
        stats.put("pending", repository.countByStatus("Pending"));
        stats.put("inProgress", repository.countByStatus("In Progress"));
        stats.put("collected", repository.countByStatus("Collected"));
        return stats;
    }

    // Admin: status maatha
    public void updateStatus(Integer id, String status) {
        repository.findById(id).ifPresent(report -> {
            report.setStatus(status);
            repository.save(report);
        });
    }

    // Admin: delete panna
    public void deleteReport(Integer id) {
        repository.deleteById(id);
    }

    // Admin: filter panna
    public List<WasteReport> filterReports(String location, String type, String status) {
        return repository.filter(location.trim(), type, status);
    }

    // Category chart-ku data
    public Map<String, Object> getCategoryData() {
        String[] names = {"Organic", "Plastic", "Paper", "Metal", "Other"};
        String[] colors = {"#22c55e", "#f59e0b", "#3b82f6", "#a855f7", "#94a3b8"};

        List<WasteReport> all = repository.findAll();
        long total = all.size();

        List<Map<String, Object>> items = new ArrayList<>();
        StringBuilder gradient = new StringBuilder();
        double start = 0;

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            long count = all.stream().filter(r -> name.equals(r.getWasteType())).count();
            double percent = total == 0 ? 0 : count * 100.0 / total;

            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("count", count);
            item.put("percent", Math.round(percent));
            item.put("color", colors[i]);
            items.add(item);

            if (count > 0) {
                if (gradient.length() > 0) {
                    gradient.append(", ");
                }
                double end = start + percent;
                gradient.append(colors[i]).append(" ").append(start).append("% ").append(end).append("%");
                start = end;
            }
        }

        if (gradient.length() == 0) {
            gradient.append("#14694c 0% 100%");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("gradient", gradient.toString());
        data.put("total", total);
        return data;
    }
}