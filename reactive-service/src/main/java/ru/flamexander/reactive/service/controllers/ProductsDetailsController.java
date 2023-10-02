package ru.flamexander.reactive.service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.flamexander.reactive.service.dtos.DetailedProductDto;
import ru.flamexander.reactive.service.dtos.ProductDetailsDto;
import ru.flamexander.reactive.service.entities.Product;
import ru.flamexander.reactive.service.services.ProductDetailsService;
import ru.flamexander.reactive.service.services.ProductsService;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/detailed")
@RequiredArgsConstructor
public class ProductsDetailsController {
    private final ProductDetailsService productDetailsService;
    private final ProductsService productsService;

    @GetMapping("/demo")
    public Flux<ProductDetailsDto> getManySlowProducts() {
        Mono<ProductDetailsDto> p1 = productDetailsService.getProductDetailsById(1L);
        Mono<ProductDetailsDto> p2 = productDetailsService.getProductDetailsById(2L);
        Mono<ProductDetailsDto> p3 = productDetailsService.getProductDetailsById(3L);
        return p1.mergeWith(p2).mergeWith(p3);
    }

    // http://localhost:8189/api/v1/detailed/1
    @GetMapping("/{id}")
    public Mono<DetailedProductDto> getProductWithDetailsById(@PathVariable Long id) {
        Mono<Product> product = productsService.findById(id);
        Mono<ProductDetailsDto> productDetails = productDetailsService.getProductDetailsById(id);
        return Mono.zip(product, productDetails)
                .map(tuple -> new DetailedProductDto(tuple.getT1().getId(), tuple.getT1().getName(), tuple.getT2().getDescription()));
    }

    // http://localhost:8189/api/v1/detailed/list?ids=1,2,3
    @GetMapping("/list")
    public Flux<DetailedProductDto> getProductWithDetailsByListId(@RequestParam String ids) {

        return Flux.fromIterable(Arrays.stream(ids.split(",")).toList())
                .flatMap(idStr -> {
                    Long id = Long.parseLong(idStr);
                    Mono<Product> product = productsService.findById(id);
                    Mono<ProductDetailsDto> productDetails = productDetailsService.getProductDetailsById(id);
                    return Mono.zip(product, productDetails)
                            .map(tuple -> new DetailedProductDto(tuple.getT1().getId(), tuple.getT1().getName(), tuple.getT2().getDescription()));
                });
    }

    // http://localhost:8189/api/v1/detailed
    @GetMapping()
    public Flux<DetailedProductDto> getProductWithDetailsByListId() {

        return productsService.findAll().flatMap(product -> {
            Mono<ProductDetailsDto> productDetails = productDetailsService.getProductDetailsById(product.getId());
            return productDetails.map(pd -> new DetailedProductDto(product.getId(), product.getName(), pd.getDescription()));
        });
    }
}
