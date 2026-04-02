package com.bartektra.product_catalog.service;

import com.bartektra.product_catalog.dto.request.ProductRequest;
import com.bartektra.product_catalog.dto.response.ProducerResponse;
import com.bartektra.product_catalog.dto.response.ProductResponse;
import com.bartektra.product_catalog.exception.ResourceNotFoundException;
import com.bartektra.product_catalog.model.Producer;
import com.bartektra.product_catalog.model.Product;
import com.bartektra.product_catalog.model.ProductAttribute;
import com.bartektra.product_catalog.repository.ProducerRepository;
import com.bartektra.product_catalog.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProducerRepository producerRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllWithDetails()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Producer producer = findProducerOrThrow(request.getProducerId());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setProducer(producer);

        setAttributes(product, request.getAttributes());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Producer producer = findProducerOrThrow(request.getProducerId());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setProducer(producer);

        product.getAttributes().clear();
        setAttributes(product, request.getAttributes());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    private Producer findProducerOrThrow(Long producerId) {
        return producerRepository.findById(producerId)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with id: " + producerId));
    }

    private void setAttributes(Product product, Map<String, String> attributeMap) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            return;
        }
        attributeMap.forEach((key, value) -> {
            ProductAttribute attribute = new ProductAttribute();
            attribute.setAttributeKey(key);
            attribute.setAttributeValue(value);
            attribute.setProduct(product);
            product.getAttributes().add(attribute);
        });
    }

    private ProducerResponse toProducerResponse(Producer producer) {
        return new ProducerResponse(
                producer.getId(),
                producer.getName(),
                producer.getDescription(),
                producer.getCountry()
        );
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setProducer(toProducerResponse(product.getProducer()));
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        Map<String, String> attributeMap = new java.util.HashMap<>();
        product.getAttributes().forEach(attr ->
                attributeMap.put(attr.getAttributeKey(), attr.getAttributeValue())
        );
        response.setAttributes(attributeMap);

        return response;
    }
}