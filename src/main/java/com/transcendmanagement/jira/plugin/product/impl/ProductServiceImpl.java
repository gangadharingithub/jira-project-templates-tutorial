package com.transcendmanagement.jira.plugin.product.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import net.java.ao.Query;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.transcendmanagement.jira.plugin.product.ProductService;
import com.transcendmanagement.jira.plugin.product.entity.Product;

@Component
public class ProductServiceImpl implements ProductService {
	private final ActiveObjects ao;

	public ProductServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Product add(String name, String version, String description) {
		final Product product = ao.create(Product.class);
		product.setName(name);
		product.setVersion(version);
		product.setDescription(description);
		product.setParentID(0);
		product.save();
		return product;
	}

	@Override
	public Product add(String name, String version, String description,
			int parentID) {
		final Product product = ao.create(Product.class);
		product.setName(name);
		product.setVersion(version);
		product.setDescription(description);
		product.setParentID(parentID);
		product.save();
		return product;
	}

	@Override
	public void delete(int ID) {
		Product toDelete = getProductById(ID);
		ao.delete(toDelete);
	}

	@Override
	public Product getProductById(int ID) {
		Product retrievedProduct = ao.find(Product.class,
				Query.select().where("ID = ?", ID))[0];
		return retrievedProduct;
	}

	@Override
	public List<Product> all() {
		return newArrayList(ao.find(Product.class));
	}

	/*** This method was not actually used ***/
	@Override
	public List<Product> taskable() {
		List<Product> taskableProducts = new ArrayList<Product>();
		List<Product> allProducts = all();
		for (int i = 0; i < allProducts.size(); i++) {
			Product currentProduct = allProducts.get(i);
			if (!hasChildren(currentProduct.getID())) {
				taskableProducts.add(currentProduct);
			}
		}
		return taskableProducts;
	}

	@Override
	public boolean hasChildren(int ID) {
		boolean has = false;
		List<Product> allProducts = all();
		for (int i = 0; i < allProducts.size(); i++) {
			Product currentProduct = allProducts.get(i);
			if (currentProduct.getParentID() == ID) {
				has = true;
			}
		}
		return has;
	}

	@Override
	public List<Product> getChildren(int ID) {
		List<Product> allProducts = all();
		List<Product> children = new ArrayList<Product>();
		for (int i = 0; i < allProducts.size(); i++) {
			Product currentProduct = allProducts.get(i);
			if (isChild(currentProduct.getID(), ID)) {
				children.add(currentProduct);
			}
		}
		return children;
	}

	@Override
	public List<Product> allParents() {
		List<Product> allProducts = all();
		List<Product> parents = new ArrayList<Product>();
		for (int i = 0; i < allProducts.size(); i++) {
			Product currentProduct = allProducts.get(i);
			if (currentProduct.getParentID() == 0) {
				parents.add(currentProduct);
			}
		}
		return parents;
	}

	public boolean isChild(int childID, int parentID) {
		Product currentProduct = ao.find(Product.class,
				Query.select().where("ID = ?", childID))[0];
		if (currentProduct.getParentID() == 0) {
			return false;
		}
		while (currentProduct.getParentID() != parentID
				&& currentProduct.getParentID() != 0) {
			if (ao.find(Product.class,
					Query.select()
							.where("ID = ?", currentProduct.getParentID())).length > 0) {
				currentProduct = ao.find(
						Product.class,
						Query.select().where("ID = ?",
								currentProduct.getParentID()))[0];
			}
		}
		if (currentProduct.getParentID() == parentID) {
			return true;
		} else {
			return false;
		}
	}

}