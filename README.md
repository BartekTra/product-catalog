# Product Catalog API

A REST API for a product catalog management system built with Spring Boot and Liquibase.
The system handles products from multiple producers, supporting products with varying numbers
of attributes — from a few basic fields up to 200+ technical specifications.

---

## Tech Stack

- **Java** 21
- **Spring Boot** 3.4.4
- **Spring Data JPA** — data access layer
- **Liquibase** — database schema management
- **H2** — in-memory database
- **Lombok** — boilerplate reduction
- **Maven** — build tool

---

## Prerequisites

- Java 21 or higher
- Maven 3.8 or higher

Verify your setup:
```bash
java -version
mvn -version
```

---

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/your-username/product-catalog.git
cd product-catalog
```

### 2. Build the project
```bash
./mvnw clean install
```

### 3. Run the application
```bash
./mvnw spring-boot:run
```

The application starts on **http://localhost:8080**

### 4. Access the H2 Console (optional)
Browse to **http://localhost:8080/h2-console** and connect with:
- **JDBC URL:** `jdbc:h2:mem:productcatalogdb`
- **Username:** `sa`
- **Password:** *(leave empty)*

---

## Design Decisions

### EAV Pattern for Product Attributes
Products in this system can have anywhere from 2 to 200+ attributes depending on their type
(e.g. a phone has color and storage, a TV has dimensions, resolution, HDMI ports, energy rating, etc.).
To handle this without wide sparse tables, the system uses the **Entity-Attribute-Value (EAV)** pattern —
a dedicated `product_attributes` table stores `(product_id, key, value)` rows. This keeps the schema
clean and flexible regardless of product complexity.

### JPA Specifications for Filtering
Product search uses Spring Data JPA **Specifications** for dynamic query building. Each filter is an
independent Specification that returns `null` when not provided, meaning only the filters actually
passed by the caller get applied. This scales cleanly to any number of filter combinations without
adding new repository methods for each case.

### Schema Ownership
Liquibase owns the database schema entirely. Hibernate is set to `ddl-auto: validate` — it only
checks that entities match the schema, never modifies it. This gives full control over migrations
and keeps the schema history clean and auditable.

---

## API Reference

### Producers

#### Get all producers
```
GET /api/producers
```
Response `200 OK`:
```json
[
  {
    "id": 1,
    "name": "Samsung",
    "description": "South Korean multinational electronics corporation",
    "country": "South Korea"
  }
]
```

#### Get producer by ID
```
GET /api/producers/{id}
```
Response `200 OK`:
```json
{
  "id": 1,
  "name": "Samsung",
  "description": "South Korean multinational electronics corporation",
  "country": "South Korea"
}
```

#### Create producer
```
POST /api/producers
```
Request body:
```json
{
  "name": "Samsung",
  "description": "South Korean multinational electronics corporation",
  "country": "South Korea"
}
```
Response `201 Created`:
```json
{
  "id": 1,
  "name": "Samsung",
  "description": "South Korean multinational electronics corporation",
  "country": "South Korea"
}
```

#### Update producer
```
PUT /api/producers/{id}
```
Request body:
```json
{
  "name": "Samsung Electronics",
  "description": "Updated description",
  "country": "South Korea"
}
```
Response `200 OK`

#### Delete producer
```
DELETE /api/producers/{id}
```
Response `204 No Content`

> **Note:** Deleting a producer that still has products attached returns `409 Conflict`.

---

### Products

#### Get all products
```
GET /api/products
```
Response `200 OK`:
```json
[
  {
    "id": 1,
    "name": "Samsung Galaxy S24",
    "description": "Latest Samsung flagship phone",
    "price": 999.99,
    "producer": {
      "id": 1,
      "name": "Samsung",
      "description": "South Korean multinational electronics corporation",
      "country": "South Korea"
    },
    "attributes": {
      "color": "Phantom Black",
      "storage": "256GB",
      "ram": "8GB",
      "battery": "4000mAh"
    },
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
]
```

#### Get product by ID
```
GET /api/products/{id}
```
Response `200 OK`

#### Create product
```
POST /api/products
```
Request body:
```json
{
  "name": "Samsung Galaxy S24",
  "description": "Latest Samsung flagship phone",
  "price": 999.99,
  "producerId": 1,
  "attributes": {
    "color": "Phantom Black",
    "storage": "256GB",
    "ram": "8GB",
    "battery": "4000mAh"
  }
}
```
Response `201 Created`

> **Note:** `attributes` is optional. Products can be created with no attributes.

#### Update product
```
PUT /api/products/{id}
```
Request body follows the same structure as Create. All existing attributes are replaced
with the ones provided in the request body.

Response `200 OK`

#### Delete product
```
DELETE /api/products/{id}
```
Response `204 No Content`

> **Note:** Deleting a product also deletes all its attributes automatically.

---

### Product Search

```
GET /api/products/search
```

All parameters are optional and combinable freely.

| Parameter | Type | Description |
|-----------|------|-------------|
| `name` | String | Case-insensitive partial match on product name |
| `producerId` | Long | Exact match on producer ID |
| `producerName` | String | Case-insensitive partial match on producer name |
| `minPrice` | Decimal | Products with price greater than or equal to value |
| `maxPrice` | Decimal | Products with price less than or equal to value |
| `attributeKey` | String | Products that have this attribute key |
| `attributeValue` | String | Combined with `attributeKey` — partial match on attribute value |

#### Example requests

Filter by name:
```
GET /api/products/search?name=galaxy
```

Filter by price range:
```
GET /api/products/search?minPrice=500&maxPrice=1200
```

Filter by producer:
```
GET /api/products/search?producerName=samsung
```

Filter by attribute key only (products that have a warranty):
```
GET /api/products/search?attributeKey=warranty_period
```

Filter by attribute key and value:
```
GET /api/products/search?attributeKey=color&attributeValue=black
```

Combine multiple filters:
```
GET /api/products/search?producerName=samsung&minPrice=500&attributeKey=color&attributeValue=black
```

---

## Error Responses

All errors follow a consistent response shape:

```json
{
  "status": 404,
  "message": "Product not found with id: 99",
  "timestamp": "2024-01-15T10:00:00"
}
```

Validation errors include a field-level breakdown:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "name": "Product name is required",
    "price": "Price must be greater than 0"
  },
  "timestamp": "2024-01-15T10:00:00"
}
```

| Status | Scenario |
|--------|----------|
| `400 Bad Request` | Invalid or missing required fields |
| `404 Not Found` | Product or producer not found |
| `409 Conflict` | Duplicate producer name or deleting a producer with products |
| `500 Internal Server Error` | Unexpected server error |

---

## Testing

The project includes a comprehensive suite of **Integration Tests** to ensure the API contracts, business logic, and database interactions work correctly together.

### Integration Tests Approach
- **Spring Boot Test & MockMvc:** Tests are written using `@SpringBootTest` combined with `@AutoConfigureMockMvc`. This allows testing the entire application context and HTTP web layer without the overhead of starting a real embedded web server.
- **Data Isolation:** The test classes (e.g., `ProductControllerIntegrationTest`) are annotated with `@Transactional`. This ensures that any database modifications made during a test (like creating, updating, or deleting a product) are automatically rolled back when the test completes, maintaining a pristine state for the next test.
- **H2 In-Memory Database:** Tests run against the H2 in-memory database, automatically applying the latest Liquibase migrations on startup.
- **Test Coverage:** The integration tests extensively cover:
  - Standard CRUD operations (`GET`, `POST`, `PUT`, `DELETE`).
  - Input validation and proper error status codes (`400 Bad Request`, `404 Not Found`, etc.).
  - Complex search scenarios and dynamic filtering using JPA Specifications.

### Running the Tests

To execute the test suite, run the following command in your terminal:

```bash
./mvnw test

## Project Structure

```
src/main/java/com/bartektra/productcatalog/
├── controller/
│   ├── ProducerController.java
│   └── ProductController.java
├── service/
│   ├── ProducerService.java
│   └── ProductService.java
├── repository/
│   ├── ProducerRepository.java
│   ├── ProductRepository.java
│   └── ProductAttributeRepository.java
├── model/
│   ├── Producer.java
│   ├── Product.java
│   └── ProductAttribute.java
├── dto/
│   ├── request/
│   │   ├── ProducerRequest.java
│   │   ├── ProductRequest.java
│   │   └── ProductFilterRequest.java
│   └── response/
│       ├── ProducerResponse.java
│       └── ProductResponse.java
├── specification/
│   └── ProductSpecification.java
└── exception/
    ├── ResourceNotFoundException.java
    ├── DuplicateResourceException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.yml
└── db/changelog/
    ├── db.changelog-master.xml
    └── changes/
        ├── 001-create-producers.xml
        ├── 002-create-products.xml
        └── 003-create-product-attributes.xml
```
