package com.gaurav.h1bchecker.controller;

import com.gaurav.h1bchecker.model.H1bResponse;
import com.gaurav.h1bchecker.service.H1bCheckerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/h1b")
public class H1bCheckerController {

    private final H1bCheckerService service;

    public H1bCheckerController(H1bCheckerService service) {
        this.service = service;
    }

    /**
     * GET /api/h1b?company=Bloomberg
     */
    @GetMapping
    public ResponseEntity<H1bResponse> check(@RequestParam String company) {
        if (company == null || company.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(H1bResponse.error("", "company param is required"));
        }
        H1bResponse result = service.lookup(company.trim());
        if (result.getError() != null) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
