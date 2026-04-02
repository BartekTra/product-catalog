package com.bartektra.product_catalog.controller;

import com.bartektra.product_catalog.dto.request.ProducerRequest;
import com.bartektra.product_catalog.dto.response.ProducerResponse;
import com.bartektra.product_catalog.service.ProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/producers")
@RequiredArgsConstructor
public class ProducerController {

    private final ProducerService producerService;

    @GetMapping
    public ResponseEntity<List<ProducerResponse>> getAllProducers() {
        return ResponseEntity.ok(producerService.getAllProducers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProducerResponse> getProducerById(@PathVariable Long id) {
        return ResponseEntity.ok(producerService.getProducerById(id));
    }

    @PostMapping
    public ResponseEntity<ProducerResponse> createProducer(
            @Valid @RequestBody ProducerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(producerService.createProducer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProducerResponse> updateProducer(
            @PathVariable Long id,
            @Valid @RequestBody ProducerRequest request) {
        return ResponseEntity.ok(producerService.updateProducer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducer(@PathVariable Long id) {
        producerService.deleteProducer(id);
        return ResponseEntity.noContent().build();
    }
}