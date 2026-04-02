package com.bartektra.product_catalog.repository;

import com.bartektra.product_catalog.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    List<Product> findByProducerId(Long producerId);

    @Query("SELECT p FROM Product p JOIN FETCH p.producer LEFT JOIN FETCH p.attributes WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(Long id);

    @Query("SELECT DISTINCT p FROM Product p JOIN FETCH p.producer LEFT JOIN FETCH p.attributes")
    List<Product> findAllWithDetails();
}