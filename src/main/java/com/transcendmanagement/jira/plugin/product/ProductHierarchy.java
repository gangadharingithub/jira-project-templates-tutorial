package com.transcendmanagement.jira.plugin.product;

import java.util.ArrayList;
import java.util.List;

import com.transcendmanagement.jira.plugin.product.entity.Product;

public class ProductHierarchy {

    public Product product;
    public List<ProductHierarchy> SubHierarchy;

    public ProductHierarchy(List<Product> allProducts, Product p)
    {
        this.product = p;

        this.SubHierarchy = this.createHierarchy(allProducts, p);
        // Find all products with this as the parent
        // Attach the sub products below this as a hierarchy
    }

    public List<ProductHierarchy> createHierarchy(List<Product> allProducts, Product p)
    {
        List<ProductHierarchy> SubHierarchy = new ArrayList<ProductHierarchy>();
        for (Product prod : allProducts)
        {
            if( prod.getParentID() == this.product.getID() )
            {
                SubHierarchy.add(new ProductHierarchy(allProducts, prod));
            }
        }

        return SubHierarchy;
    }

    public Product getProduct()
    {
        return this.product;
    }

    public List<ProductHierarchy> getSubHierarchy()
    {
        return this.SubHierarchy;
    }
}
