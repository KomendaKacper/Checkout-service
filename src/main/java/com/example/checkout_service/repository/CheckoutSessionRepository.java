package com.example.checkout_service.repository;

import com.example.checkout_service.model.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, String> {
}
