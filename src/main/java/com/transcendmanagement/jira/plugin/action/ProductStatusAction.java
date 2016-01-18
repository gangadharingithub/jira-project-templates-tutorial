package com.transcendmanagement.jira.plugin.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import webwork.action.ActionContext;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.transcendmanagement.jira.plugin.product.ProductService;
import com.transcendmanagement.jira.plugin.product.entity.Product;
import com.transcendmanagement.jira.plugin.product.impl.ProductServiceImpl;

public class ProductStatusAction extends JiraWebActionSupport {
	private ActiveObjects ao;
	private ProductService productService;
	private Product product;

	public ProductStatusAction(ActiveObjects activeObject) {		
		
		this.ao = activeObject;
		this.productService = new ProductServiceImpl(ao);
		// this.hierarchy = this.createHierarchy(service.all());
	}
	
	@Override
    public String execute() throws Exception {
		
		HttpServletRequest req = ActionContext.getRequest();
		String productId = req.getParameter("productId");
		String parentId = req.getParameter("parentID");
		String pkey = req.getParameter("projectKey");
		Product product;
		
		if(!StringUtils.isEmpty(req.getParameter("name"))
				&&!StringUtils.isEmpty(req.getParameter("version"))
					&&!StringUtils.isEmpty(req.getParameter("description"))){
			
			//edit
			if(Integer.parseInt(productId) > 0){
				
				product = productService.getProductById(Integer.parseInt(productId));
				product.setName(req.getParameter("name"));
				product.setVersion(req.getParameter("version"));
				product.setDescription(req.getParameter("description"));
				product.save();
				
			//create a leaf/branch node	
			}else if(Integer.parseInt(productId) == 0 && !StringUtils.isEmpty(parentId)){
				
				productService.add(req.getParameter("name"),
						req.getParameter("version"),
						req.getParameter("description"),
						Integer.parseInt(parentId));
				
			}
		}else{
			
			//remove case
			productService.delete(Integer.parseInt(productId));
		}
		
		return getRedirect("/browse/" + pkey + "/?selectedTab=com.transcendmanagement.jira.plugin.jira-project-templates-plugin:my.project.product.tabpanel", true);
    }
	
   /* public String doGet() {
		
		HttpServletRequest req = ActionContext.getRequest();
    	
    	String pkey = req.getParameter("projectKey");
    	
    	
    	 return getRedirect("/browse/" + pkey + "/?selectedTab=com.transcendmanagement.jira.plugin.jira-project-templates-plugin:my.project.product.tabpanel", true);
    }*/
	
   /* public String update() {
		
		HttpServletRequest req = ActionContext.getRequest();
    	
    	String pkey = req.getParameter("projectKey");
    	
    	productService.add(req.getParameter("name"),
				req.getParameter("version"),
				req.getParameter("description"),
				Integer.parseInt(req.getParameter("parentID")));    	
    	
    	 return getRedirect("/browse/" + pkey + "/?selectedTab=com.transcendmanagement.jira.plugin.jira-project-templates-plugin:my.project.product.tabpanel", true);
    }*/
	
	
	
}
