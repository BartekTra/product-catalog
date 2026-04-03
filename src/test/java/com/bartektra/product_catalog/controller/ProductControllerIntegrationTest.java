package com.bartektra.product_catalog.controller;

import com.bartektra.product_catalog.dto.request.ProductRequest;
import com.bartektra.product_catalog.model.Producer;
import com.bartektra.product_catalog.model.Product;
import com.bartektra.product_catalog.model.ProductAttribute;
import com.bartektra.product_catalog.repository.ProducerRepository;
import com.bartektra.product_catalog.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProducerRepository producerRepository;

    private Producer samsung;
    private Producer apple;
    private Product galaxyS24;
    private Product iphone;

    @BeforeEach
    void setUp() {
        samsung = new Producer();
        samsung.setName("Samsung");
        samsung.setCountry("South Korea");
        samsung = producerRepository.save(samsung);

        apple = new Producer();
        apple.setName("Apple");
        apple.setCountry("USA");
        apple = producerRepository.save(apple);

        galaxyS24 = new Product();
        galaxyS24.setName("Samsung Galaxy S24");
        galaxyS24.setDescription("Samsung flagship phone");
        galaxyS24.setPrice(new BigDecimal("999.99"));
        galaxyS24.setProducer(samsung);
        galaxyS24.setAttributes(new ArrayList<>());
        addAttribute(galaxyS24, "color", "Phantom Black");
        addAttribute(galaxyS24, "storage", "256GB");
        addAttribute(galaxyS24, "warranty_period", "2 years");
        galaxyS24 = productRepository.save(galaxyS24);

        iphone = new Product();
        iphone.setName("iPhone 15 Pro");
        iphone.setDescription("Apple flagship phone");
        iphone.setPrice(new BigDecimal("1199.99"));
        iphone.setProducer(apple);
        iphone.setAttributes(new ArrayList<>());
        addAttribute(iphone, "color", "Natural Titanium");
        addAttribute(iphone, "storage", "256GB");
        addAttribute(iphone, "warranty_period", "1 year");
        iphone = productRepository.save(iphone);
    }

    private void addAttribute(Product product, String key, String value) {
        ProductAttribute attr = new ProductAttribute();
        attr.setAttributeKey(key);
        attr.setAttributeValue(value);
        attr.setProduct(product);
        product.getAttributes().add(attr);
    }

    // -------------------------------------------------------------------------
    // GET /api/products
    // -------------------------------------------------------------------------

    @Test
    void getAllProducts_shouldReturn200WithAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].name", hasItems("Samsung Galaxy S24", "iPhone 15 Pro")));
    }

    @Test
    void getAllProducts_shouldIncludeProducerDetails() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].producer").exists())
                .andExpect(jsonPath("$[*].producer.name", hasItems("Samsung", "Apple")));
    }

    @Test
    void getAllProducts_shouldIncludeAttributes() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].attributes").exists());
    }

    // -------------------------------------------------------------------------
    // GET /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    void getProductById_shouldReturn200WithFullDetails() throws Exception {
        mockMvc.perform(get("/api/products/{id}", galaxyS24.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(galaxyS24.getId()))
                .andExpect(jsonPath("$.name").value("Samsung Galaxy S24"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.producer.name").value("Samsung"))
                .andExpect(jsonPath("$.attributes.color").value("Phantom Black"))
                .andExpect(jsonPath("$.attributes.storage").value("256GB"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void getProductById_shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    // -------------------------------------------------------------------------
    // POST /api/products
    // -------------------------------------------------------------------------

    @Test
    void createProduct_shouldReturn201WithCreatedProduct() throws Exception {
        ProductRequest request = new ProductRequest(
                "Samsung QLED TV",
                "65 inch TV",
                new BigDecimal("1499.99"),
                samsung.getId(),
                Map.of("resolution", "4K", "size", "65 inch")
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Samsung QLED TV"))
                .andExpect(jsonPath("$.price").value(1499.99))
                .andExpect(jsonPath("$.producer.id").value(samsung.getId()))
                .andExpect(jsonPath("$.attributes.resolution").value("4K"))
                .andExpect(jsonPath("$.attributes.size").value("65 inch"));
    }

    @Test
    void createProduct_shouldReturn201WithNoAttributes() throws Exception {
        ProductRequest request = new ProductRequest(
                "Basic Product",
                "No attributes",
                new BigDecimal("49.99"),
                samsung.getId(),
                Map.of()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attributes").isEmpty());
    }

    @Test
    void createProduct_shouldReturn404WhenProducerNotFound() throws Exception {
        ProductRequest request = new ProductRequest(
                "Ghost Product",
                "No producer",
                new BigDecimal("99.99"),
                999L,
                Map.of()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producer not found with id: 999"));
    }

    @Test
    void createProduct_shouldReturn400WhenNameIsMissing() throws Exception {
        ProductRequest request = new ProductRequest(
                null, "No name", new BigDecimal("99.99"), samsung.getId(), Map.of()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createProduct_shouldReturn400WhenPriceIsNegative() throws Exception {
        ProductRequest request = new ProductRequest(
                "Bad Product", "Negative price", new BigDecimal("-1.00"), samsung.getId(), Map.of()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void createProduct_shouldReturn400WhenPriceIsMissing() throws Exception {
        ProductRequest request = new ProductRequest(
                "No Price Product", "Missing price", null, samsung.getId(), Map.of()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void createProduct_shouldReturn400WhenProducerIdIsMissing() throws Exception {
        ProductRequest request = new ProductRequest(
                "No Producer", "Missing producerId", new BigDecimal("99.99"), null, Map.of()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.producerId").exists());
    }

    // -------------------------------------------------------------------------
    // PUT /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    void updateProduct_shouldReturn200WithUpdatedFields() throws Exception {
        ProductRequest request = new ProductRequest(
                "Samsung Galaxy S24 Ultra",
                "Updated flagship",
                new BigDecimal("1299.99"),
                samsung.getId(),
                Map.of("color", "Titanium Gray", "storage", "512GB")
        );

        mockMvc.perform(put("/api/products/{id}", galaxyS24.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Samsung Galaxy S24 Ultra"))
                .andExpect(jsonPath("$.price").value(1299.99))
                .andExpect(jsonPath("$.attributes.color").value("Titanium Gray"))
                .andExpect(jsonPath("$.attributes.storage").value("512GB"));
    }

    @Test
    void updateProduct_shouldReplaceAllAttributes() throws Exception {
        ProductRequest request = new ProductRequest(
                "Samsung Galaxy S24",
                "Same name",
                new BigDecimal("999.99"),
                samsung.getId(),
                Map.of("newAttr", "newValue")
        );

        mockMvc.perform(put("/api/products/{id}", galaxyS24.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes.newAttr").value("newValue"))
                .andExpect(jsonPath("$.attributes.color").doesNotExist())
                .andExpect(jsonPath("$.attributes.storage").doesNotExist());
    }

    @Test
    void updateProduct_shouldAllowChangingProducer() throws Exception {
        ProductRequest request = new ProductRequest(
                "Samsung Galaxy S24",
                "Now Apple's product",
                new BigDecimal("999.99"),
                apple.getId(),
                Map.of()
        );

        mockMvc.perform(put("/api/products/{id}", galaxyS24.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.producer.name").value("Apple"));
    }

    @Test
    void updateProduct_shouldReturn404WhenProductNotFound() throws Exception {
        ProductRequest request = new ProductRequest(
                "Ghost", "Not found", new BigDecimal("99.99"), samsung.getId(), Map.of()
        );

        mockMvc.perform(put("/api/products/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void updateProduct_shouldReturn404WhenProducerNotFound() throws Exception {
        ProductRequest request = new ProductRequest(
                "Product", "Bad producer", new BigDecimal("99.99"), 999L, Map.of()
        );

        mockMvc.perform(put("/api/products/{id}", galaxyS24.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producer not found with id: 999"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    void deleteProduct_shouldReturn204WhenDeleted() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", galaxyS24.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_shouldReturn404OnSubsequentGet() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", galaxyS24.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", galaxyS24.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    // -------------------------------------------------------------------------
    // GET /api/products/search
    // -------------------------------------------------------------------------

    @Test
    void searchProducts_shouldReturnAllWhenNoFilters() throws Exception {
        mockMvc.perform(get("/api/products/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void searchProducts_shouldFilterByNamePartialMatch() throws Exception {
        mockMvc.perform(get("/api/products/search").param("name", "galaxy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Samsung Galaxy S24"));
    }

    @Test
    void searchProducts_shouldFilterByNameCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/products/search").param("name", "IPHONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("iPhone 15 Pro"));
    }

    @Test
    void searchProducts_shouldFilterByProducerId() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("producerId", samsung.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].producer.name").value("Samsung"));
    }

    @Test
    void searchProducts_shouldFilterByProducerName() throws Exception {
        mockMvc.perform(get("/api/products/search").param("producerName", "apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("iPhone 15 Pro"));
    }

    @Test
    void searchProducts_shouldFilterByMinPrice() throws Exception {
        mockMvc.perform(get("/api/products/search").param("minPrice", "1100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("iPhone 15 Pro"));
    }

    @Test
    void searchProducts_shouldFilterByMaxPrice() throws Exception {
        mockMvc.perform(get("/api/products/search").param("maxPrice", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Samsung Galaxy S24"));
    }

    @Test
    void searchProducts_shouldFilterByPriceRange() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("minPrice", "900")
                        .param("maxPrice", "1100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Samsung Galaxy S24"));
    }

    @Test
    void searchProducts_shouldFilterByAttributeKeyOnly() throws Exception {
        mockMvc.perform(get("/api/products/search").param("attributeKey", "warranty_period"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchProducts_shouldFilterByAttributeKeyAndValue() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("attributeKey", "color")
                        .param("attributeValue", "phantom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Samsung Galaxy S24"));
    }

    @Test
    void searchProducts_shouldReturnEmptyListWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/products/search").param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchProducts_shouldCombineMultipleFilters() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("producerName", "samsung")
                        .param("minPrice", "500")
                        .param("attributeKey", "color")
                        .param("attributeValue", "black"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Samsung Galaxy S24"));
    }

    @Test
    void searchProducts_shouldReturn400WhenMinPriceIsNegative() throws Exception {
        mockMvc.perform(get("/api/products/search").param("minPrice", "-10"))
                .andExpect(status().isBadRequest());
    }
}