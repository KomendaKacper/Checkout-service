package com.example.checkout_service.repository;

import com.example.checkout_service.model.MultiPriceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MultiPriceRuleRepository extends JpaRepository<MultiPriceRule, Long> {
    Optional<MultiPriceRule> findBySku(String sku);
}
