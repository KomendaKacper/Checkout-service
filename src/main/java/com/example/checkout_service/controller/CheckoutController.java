package com.example.checkout_service.controller;
import com.example.checkout_service.dto.ReceiptDTO;
import com.example.checkout_service.dto.ScanResponseDTO;
import com.example.checkout_service.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/session")
    public String startSession() {
        return checkoutService.startSession();
    }

    @PostMapping("/{sessionId}/scan/{sku}")
    public ResponseEntity<ScanResponseDTO> scan(@PathVariable String sessionId, @PathVariable String sku) {
        checkoutService.scan(sessionId, sku);

        var cart = checkoutService.getCart(sessionId);
        var addedItem = cart.stream()
                .filter(i -> i.getProduct().getSku().equals(sku))
                .findFirst()
                .orElseThrow();

        ScanResponseDTO response = new ScanResponseDTO(
                addedItem.getProduct().getSku(),
                addedItem.getProduct().getName(),
                addedItem.getQuantity()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/receipt")
    public ResponseEntity<ReceiptDTO> checkout(@PathVariable String sessionId) {
        return ResponseEntity.ok(checkoutService.checkout(sessionId));
    }
}
