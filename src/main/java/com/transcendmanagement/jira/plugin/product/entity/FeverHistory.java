package com.transcendmanagement.jira.plugin.product.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface FeverHistory extends Entity
{
    String getDate();
	int getProbability();
    String getProductName();

    void setDate(String date);
    void setProbability(int probability);
    void setProductName(String name);
}