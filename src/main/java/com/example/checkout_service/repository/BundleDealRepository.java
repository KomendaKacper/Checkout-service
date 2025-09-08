package com.example.checkout_service.repository;

import com.example.checkout_service.model.BundleDeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BundleDealRepository extends JpaRepository<BundleDeal, Long> {
    List<BundleDeal> findAll();
}
