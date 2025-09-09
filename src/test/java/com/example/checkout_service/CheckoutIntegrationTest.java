package com.example.checkout_service;

import com.example.checkout_service.model.BundleDeal;
import com.example.checkout_service.model.MultiPriceRule;
import com.example.checkout_service.model.Product;
import com.example.checkout_service.repository.BundleDealRepository;
import com.example.checkout_service.repository.CheckoutSessionRepository;
import com.example.checkout_service.repository.MultiPriceRuleRepository;
import com.example.checkout_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.math.BigDecimal;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class CheckoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MultiPriceRuleRepository multiPriceRuleRepository;

    @Autowired
    private BundleDealRepository bundleDealRepository;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @BeforeEach
    void setUp() {
        checkoutSessionRepository.deleteAll();
        productRepository.deleteAll();
        multiPriceRuleRepository.deleteAll();
        bundleDealRepository.deleteAll();

        productRepository.deleteAll();
        multiPriceRuleRepository.deleteAll();
        bundleDealRepository.deleteAll();

        productRepository.save(new Product(null, "A", "Product A", BigDecimal.valueOf(40)));
        productRepository.save(new Product(null, "B", "Product B", BigDecimal.valueOf(10)));

        multiPriceRuleRepository.save(new MultiPriceRule(null, "A", 3, BigDecimal.valueOf(30)));

        bundleDealRepository.save(new BundleDeal(null, "A", "B", BigDecimal.valueOf(2.5)));
    }

    @Test
    void testCheckoutWithDiscounts() throws Exception {
        String sessionId = mockMvc.perform(post("/checkout/session"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(post("/checkout/" + sessionId + "/scan/A"));
        mockMvc.perform(post("/checkout/" + sessionId + "/scan/A"));
        mockMvc.perform(post("/checkout/" + sessionId + "/scan/A"));
        mockMvc.perform(post("/checkout/" + sessionId + "/scan/B"));

        mockMvc.perform(get("/checkout/" + sessionId + "/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].discountedUnitPrice").value(30))
                .andExpect(jsonPath("$.items[0].totalPrice").value(90))
                .andExpect(jsonPath("$.appliedBundles[0].sku1").value("A"))
                .andExpect(jsonPath("$.appliedBundles[0].sku2").value("B"))
                .andExpect(jsonPath("$.appliedBundles[0].discount").value(2.5))
                .andExpect(jsonPath("$.total").value(97.5));
    }

    @Test
    void testScanUnknownSku() throws Exception {
        String sessionId = mockMvc.perform(post("/checkout/session"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(post("/checkout/" + sessionId + "/scan/X"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown SKU: X"));
    }

    @Test
    void testScanInvalidSession() throws Exception {
        mockMvc.perform(post("/checkout/invalid-session/scan/A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Session not found"));
    }
}
