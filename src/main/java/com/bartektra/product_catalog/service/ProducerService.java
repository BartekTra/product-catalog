package com.bartektra.product_catalog.service;

import com.bartektra.product_catalog.dto.request.ProducerRequest;
import com.bartektra.product_catalog.dto.response.ProducerResponse;
import com.bartektra.product_catalog.model.Producer;
import com.bartektra.product_catalog.repository.ProducerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProducerService {

    private final ProducerRepository producerRepository;

    @Transactional(readOnly = true)
    public List<ProducerResponse> getAllProducers() {
        return producerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProducerResponse getProducerById(Long id) {
        Producer producer = findProducerOrThrow(id);
        return toResponse(producer);
    }

    @Transactional
    public ProducerResponse createProducer(ProducerRequest request) {
        if (producerRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "Producer with name '" + request.getName() + "' already exists"
            );
        }
        Producer producer = toEntity(request);
        return toResponse(producerRepository.save(producer));
    }

    @Transactional
    public ProducerResponse updateProducer(Long id, ProducerRequest request) {
        Producer producer = findProducerOrThrow(id);

        if (!producer.getName().equalsIgnoreCase(request.getName())
                && producerRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "Producer with name '" + request.getName() + "' already exists"
            );
        }

        producer.setName(request.getName());
        producer.setDescription(request.getDescription());
        producer.setCountry(request.getCountry());

        return toResponse(producerRepository.save(producer));
    }

    @Transactional
    public void deleteProducer(Long id) {
        Producer producer = findProducerOrThrow(id);
        producerRepository.delete(producer);
    }

    private Producer findProducerOrThrow(Long id) {
        return producerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Producer not found with id: " + id
                ));
    }

    private Producer toEntity(ProducerRequest request) {
        Producer producer = new Producer();
        producer.setName(request.getName());
        producer.setDescription(request.getDescription());
        producer.setCountry(request.getCountry());
        return producer;
    }

    private ProducerResponse toResponse(Producer producer) {
        return new ProducerResponse(
                producer.getId(),
                producer.getName(),
                producer.getDescription(),
                producer.getCountry()
        );
    }
}