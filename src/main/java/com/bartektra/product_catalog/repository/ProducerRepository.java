package com.bartektra.product_catalog.repository;

import com.bartektra.product_catalog.model.Producer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProducerRepository extends JpaRepository<Producer, Long> {

    Optional<Producer> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}