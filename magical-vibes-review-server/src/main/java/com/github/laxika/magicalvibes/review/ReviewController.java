package com.github.laxika.magicalvibes.review;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** HTTP boundary shared by the dashboard and independent checkout workers. */
@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() { return service.overview(); }

    @GetMapping("/runs")
    public List<Map<String, Object>> runs() { return service.runs(); }

    @PostMapping("/runs")
    public ResponseEntity<Map<String, Object>> create(@RequestBody ReviewService.NewRun request) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRun(request));
    }

    @GetMapping("/runs/{id}")
    public Map<String, Object> run(@PathVariable long id) { return service.run(id); }

    @PutMapping("/active-run")
    public ResponseEntity<Void> activate(@RequestBody ActiveRun request) {
        service.activate(request.runId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/claim")
    public ResponseEntity<Map<String, Object>> claim(@RequestBody ReviewService.Claim request) {
        var task = service.claim(request);
        return task == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(task);
    }

    @PostMapping("/tasks/{id}/result")
    public ResponseEntity<Void> submit(@PathVariable long id, @RequestBody ReviewService.Result result) {
        service.submit(id, result);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{id}/requeue")
    public ResponseEntity<Void> requeue(@PathVariable long id) {
        service.requeue(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/runs/{id}/requeue")
    public Map<String, Integer> requeueRun(@PathVariable long id, @RequestBody Requeue request) {
        return Map.of("requeued", service.requeueRun(id, request.status()));
    }

    @GetMapping("/runs/{id}/tasks")
    public Map<String, Object> tasks(@PathVariable long id, @RequestParam(defaultValue = "") String query,
                                     @RequestParam(defaultValue = "") String status, @RequestParam(defaultValue = "0") int page) {
        return service.tasks(id, query, status, page);
    }

    @GetMapping("/tasks/{id}")
    public Map<String, Object> task(@PathVariable long id) { return service.task(id); }

    @GetMapping("/cards")
    public Map<String, Object> cards(@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "0") int page) {
        return service.cards(query, page);
    }

    @GetMapping("/cards/{id}")
    public Map<String, Object> card(@PathVariable long id) { return service.card(id); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> invalid(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("error", exception.getReason() == null ? "Invalid request" : exception.getReason()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> unavailable(IOException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not read the card catalog: " + exception.getMessage()));
    }

    public record ActiveRun(Long runId) {}
    public record Requeue(String status) {}

    /** Supplies a single transactional boundary implementation for the JDBC service. */
    @Configuration
    static class Transactions {
        @Bean
        TransactionTemplate reviewTransactions(PlatformTransactionManager manager) {
            return new TransactionTemplate(manager);
        }
    }
}
