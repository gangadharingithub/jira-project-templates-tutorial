package com.transcendmanagement.jira.plugin.product;

import java.util.List;

import com.atlassian.activeobjects.tx.Transactional;
import com.transcendmanagement.jira.plugin.product.entity.Product;

@Transactional
public interface ProductService {
	Product add(String name, String version, String description);

	Product add(String name, String version, String description, int parentID);

	Product getProductById(int ID);

	void delete(int ID);

	List<Product> all();

	List<Product> taskable(); // Taskable means only the lowest level products
								// in the hierarchy. This was not used.

	boolean hasChildren(int ID);

	List<Product> getChildren(int ID);

	List<Product> allParents();
}
