package com.bartektra.product_catalog.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    private String name;

    private Long producerId;

    private String producerName;

    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum price must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid price format")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum price must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid price format")
    private BigDecimal maxPrice;

    private String attributeKey;

    private String attributeValue;
}