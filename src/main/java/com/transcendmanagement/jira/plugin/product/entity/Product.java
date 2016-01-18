package com.transcendmanagement.jira.plugin.product.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
 
@Preload
public interface Product extends Entity
{
    String getName();
	String getDescription();
    int getParentID();
    String getVersion();

    void setName(String name);
    void setDescription(String description);
    void setParentID(int id);
    void setVersion(String version);

}