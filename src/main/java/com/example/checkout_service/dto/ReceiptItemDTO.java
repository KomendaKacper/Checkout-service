package com.example.checkout_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ReceiptItemDTO {
    private String name;
    private BigDecimal unitPrice;
    private BigDecimal discountedUnitPrice;
    private int quantity;
    private BigDecimal totalPrice;
}
