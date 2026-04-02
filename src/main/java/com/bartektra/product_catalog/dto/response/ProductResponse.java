package com.bartektra.product_catalog.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProducerResponse producer;
    private Map<String, String> attributes = new HashMap<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}