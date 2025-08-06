package com.product.productservice.controller;

import com.product.productservice.model.Product;
import com.product.productservice.service.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produits")
// Permet au front-end React d'appeler cette API
@CrossOrigin(origins = "https://frontend-app-usut6o4i3q-ew.a.run.app/") //http://localhost:3000
public class ProductController {

    @Autowired
    private ProductRepository produitRepository;

    @GetMapping
    public List<Product> getAllProduits() {
        return produitRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProduitById(@PathVariable Long id) {
        return produitRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Product createProduit(@RequestBody Product produit) {
        return produitRepository.save(produit);
    }
}
