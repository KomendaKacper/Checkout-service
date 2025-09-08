package com.example.checkout_service;

import com.example.checkout_service.dto.ReceiptDTO;
import com.example.checkout_service.model.CartItem;
import com.example.checkout_service.model.CheckoutSession;
import com.example.checkout_service.model.MultiPriceRule;
import com.example.checkout_service.model.Product;
import com.example.checkout_service.repository.BundleDealRepository;
import com.example.checkout_service.repository.CheckoutSessionRepository;
import com.example.checkout_service.repository.MultiPriceRuleRepository;
import com.example.checkout_service.repository.ProductRepository;
import com.example.checkout_service.service.CheckoutService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceUnitTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    CheckoutSessionRepository sessionRepository;
    @Mock
    MultiPriceRuleRepository multiPriceRepository;
    @Mock
    BundleDealRepository bundleRepository;

    @InjectMocks
    CheckoutService checkoutService;

    @Test
    void testMultiPriceApplied() {
        Product productA = new Product(null, "A", "Product A", BigDecimal.valueOf(40));

        CartItem item = new CartItem(null, productA, 3);

        CheckoutSession session = new CheckoutSession();
        session.setSessionId("sess1");
        session.setItems(List.of(item));

        Mockito.when(sessionRepository.findById("sess1")).thenReturn(Optional.of(session));
        Mockito.when(multiPriceRepository.findBySku("A")).thenReturn(Optional.of(new MultiPriceRule(null, "A", 3, BigDecimal.valueOf(30))));

        ReceiptDTO receipt = checkoutService.checkout("sess1");

        Assertions.assertEquals(1, receipt.getItems().size());
        Assertions.assertEquals(BigDecimal.valueOf(30), receipt.getItems().get(0).getDiscountedUnitPrice());
        Assertions.assertEquals(BigDecimal.valueOf(90), receipt.getItems().get(0).getTotalPrice());
    }

    @Test
    void testNoMultiPriceRule() {
        Product productB = new Product(null, "B", "Product B", BigDecimal.valueOf(10));
        CartItem item = new CartItem(null, productB, 2);

        CheckoutSession session = new CheckoutSession();
        session.setSessionId("sess2");
        session.setItems(List.of(item));

        Mockito.when(sessionRepository.findById("sess2")).thenReturn(Optional.of(session));
        Mockito.when(multiPriceRepository.findBySku("B")).thenReturn(Optional.empty());

        ReceiptDTO receipt = checkoutService.checkout("sess2");

        Assertions.assertEquals(BigDecimal.valueOf(20), receipt.getItems().get(0).getTotalPrice());
        Assertions.assertEquals(BigDecimal.valueOf(20), receipt.getTotal());
    }

    @Test
    void testMultiPriceNotEnoughQuantity() {
        Product productA = new Product(null, "A", "Product A", BigDecimal.valueOf(40));
        CartItem item = new CartItem(null, productA, 2);

        CheckoutSession session = new CheckoutSession();
        session.setSessionId("sess3");
        session.setItems(List.of(item));

        Mockito.when(sessionRepository.findById("sess3")).thenReturn(Optional.of(session));
        Mockito.when(multiPriceRepository.findBySku("A")).thenReturn(Optional.of(new MultiPriceRule(null, "A", 3, BigDecimal.valueOf(30))));

        ReceiptDTO receipt = checkoutService.checkout("sess3");

        Assertions.assertEquals(BigDecimal.valueOf(80), receipt.getItems().get(0).getTotalPrice());
        Assertions.assertEquals(BigDecimal.valueOf(80), receipt.getTotal());
    }

    @Test
    void testBundleApplied() {
        Product productA = new Product(null, "A", "Product A", BigDecimal.valueOf(40));
        Product productB = new Product(null, "B", "Product B", BigDecimal.valueOf(10));

        CartItem itemA = new CartItem(null, productA, 1);
        CartItem itemB = new CartItem(null, productB, 1);

        CheckoutSession session = new CheckoutSession();
        session.setSessionId("sess4");
        session.setItems(List.of(itemA, itemB));

        Mockito.when(sessionRepository.findById("sess4")).thenReturn(Optional.of(session));
        Mockito.when(multiPriceRepository.findBySku(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(bundleRepository.findAll()).thenReturn(List.of(
                new com.example.checkout_service.model.BundleDeal(null, "A", "B", BigDecimal.valueOf(2.5))
        ));

        ReceiptDTO receipt = checkoutService.checkout("sess4");

        Assertions.assertEquals(BigDecimal.valueOf(47.5), receipt.getTotal());
        Assertions.assertEquals(1, receipt.getAppliedBundles().size());
        Assertions.assertEquals(BigDecimal.valueOf(2.5), receipt.getAppliedBundles().get(0).getDiscount());
    }

    @Test
    void testScanUnknownSku() {
        Mockito.when(productRepository.findBySku("X")).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            checkoutService.scan("sess5", "X");
        });
    }

    @Test
    void testCheckoutSessionNotFound() {
        Mockito.when(sessionRepository.findById("missing1")).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            checkoutService.checkout("missing1");
        });
    }

    @Test
    void testGetCartSessionNotFound() {
        Mockito.when(sessionRepository.findById("missing2")).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            checkoutService.getCart("missing2");
        });
    }

}
