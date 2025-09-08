package com.example.checkout_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScanResponseDTO {
    private String sku;
    private String name;
    private int quantity;
}
