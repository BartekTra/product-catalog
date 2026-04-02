package com.bartektra.product_catalog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(
        name = "product_attributes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_product_attribute_key",
                columnNames = {"product_id", "attribute_key"}
        )
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "attribute_key", nullable = false)
    private String attributeKey;

    @Column(name = "attribute_value", length = 1000)
    private String attributeValue;
}