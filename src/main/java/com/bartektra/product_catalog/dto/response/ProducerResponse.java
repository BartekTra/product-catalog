package com.bartektra.product_catalog.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProducerResponse {

    private Long id;
    private String name;
    private String description;
    private String country;
}