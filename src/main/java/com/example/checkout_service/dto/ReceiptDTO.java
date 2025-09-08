package com.example.checkout_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReceiptDTO {
    private List<ReceiptItemDTO> items;
    private List<BundleAppliedDTO> appliedBundles;
    private BigDecimal total;
}


