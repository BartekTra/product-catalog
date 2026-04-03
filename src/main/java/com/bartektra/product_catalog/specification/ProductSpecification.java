package com.bartektra.product_catalog.specification;

import com.bartektra.product_catalog.model.Producer;
import com.bartektra.product_catalog.model.Product;
import com.bartektra.product_catalog.model.ProductAttribute;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasProducerId(Long producerId) {
        return (root, query, cb) -> {
            if (producerId == null) return null;
            Join<Product, Producer> producer = root.join("producer", JoinType.INNER);
            return cb.equal(producer.get("id"), producerId);
        };
    }

    public static Specification<Product> hasProducerNameLike(String producerName) {
        return (root, query, cb) -> {
            if (producerName == null || producerName.isBlank()) return null;
            Join<Product, Producer> producer = root.join("producer", JoinType.INNER);
            return cb.like(cb.lower(producer.get("name")), "%" + producerName.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) return null;
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Product> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) return null;
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Product> hasAttribute(String key, String value) {
        return (root, query, cb) -> {
            if (key == null || key.isBlank()) return null;
            Join<Product, ProductAttribute> attributes = root.join("attributes", JoinType.INNER);
            Predicate keyPredicate = cb.equal(
                    cb.lower(attributes.get("attributeKey")),
                    key.toLowerCase()
            );
            if (value == null || value.isBlank()) {
                return keyPredicate;
            }
            Predicate valuePredicate = cb.like(
                    cb.lower(attributes.get("attributeValue")),
                    "%" + value.toLowerCase() + "%"
            );
            return cb.and(keyPredicate, valuePredicate);
        };
    }

    public static Specification<Product> buildFilter(
            String name,
            Long producerId,
            String producerName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String attributeKey,
            String attributeValue) {

        return Specification.where(hasNameLike(name))
                .and(hasProducerId(producerId))
                .and(hasProducerNameLike(producerName))
                .and(hasPriceGreaterThanOrEqual(minPrice))
                .and(hasPriceLessThanOrEqual(maxPrice))
                .and(hasAttribute(attributeKey, attributeValue));
    }
}