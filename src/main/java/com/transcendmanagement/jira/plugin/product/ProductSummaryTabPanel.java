package com.transcendmanagement.jira.plugin.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.transcendmanagement.jira.plugin.product.entity.Product;
import com.transcendmanagement.jira.plugin.product.impl.ProductServiceImpl;

public class ProductSummaryTabPanel extends AbstractProjectTabPanel implements
		ProjectTabPanel {

	private static final Logger log = LoggerFactory
			.getLogger(ProductSummaryTabPanel.class);
	
	private ActiveObjects ao;
	private ProductService service;
	private List<ProductHierarchy> hierarchy;
	//private PageBuilderService pageBuilder;
	protected static final String WEB_RESOURCE_KEY1 = "com.transcendmanagement.jira.plugin.jira-project-templates-plugin:my-project-template-resources";
	protected static final String WEB_RESOURCE_KEY2 = "com.transcendmanagement.jira.plugin.jira-project-templates-plugin:jira-project-templates-plugin-resources";
	

	public boolean showPanel(BrowseContext context) {
		return true;
	}

	public ProductSummaryTabPanel(ActiveObjects activeObject,
			PageBuilderService pageBuilder) {
		this.ao = activeObject;	
		this.service = new ProductServiceImpl(ao);
		this.hierarchy = this.createHierarchy(service.all());
		pageBuilder.assembler().resources().requireWebResource(WEB_RESOURCE_KEY1);
		pageBuilder.assembler().resources().requireWebResource(WEB_RESOURCE_KEY2);
	}

	@Override
	protected Map<String, Object> createVelocityParams(BrowseContext ctx) {
		Map<String, Object> params = super.createVelocityParams(ctx);		
		params.put("hierarchyList", this.hierarchy);		
		return params;
	}

	@Override
	public String getHtml(BrowseContext ctx) {
		Map<String, Object> params = super.createVelocityParams(ctx);
		this.hierarchy = this.createHierarchy(service.all());
		String projectKey = ctx.getProject().getKey();
        params.put("projectKey", projectKey);
		params.put("hierarchyList", this.hierarchy);		
		return descriptor.getHtml("view", params);
	}

	public List<ProductHierarchy> createHierarchy(List<Product> allProducts) {
		List<ProductHierarchy> productHierarchy = new ArrayList<ProductHierarchy>();
		for (Product prod : allProducts) {
			if (prod.getParentID() == 0) {
				productHierarchy.add(new ProductHierarchy(allProducts, prod));
			}
		}
		return productHierarchy;
	}

}
