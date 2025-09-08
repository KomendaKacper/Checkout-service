package com.example.checkout_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class BundleAppliedDTO {

    private String sku1;
    private String sku2;
    private int quantity;
    private BigDecimal discount;
}
