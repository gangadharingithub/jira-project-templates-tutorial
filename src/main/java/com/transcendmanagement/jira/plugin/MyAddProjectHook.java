package com.transcendmanagement.jira.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.blueprint.api.AddProjectHook;
import com.atlassian.jira.blueprint.api.ConfigureData;
import com.atlassian.jira.blueprint.api.ConfigureResponse;
import com.atlassian.jira.blueprint.api.EmptyAddProjectHook;
import com.atlassian.jira.blueprint.api.ValidateData;
import com.atlassian.jira.blueprint.api.ValidateResponse;
import com.transcendmanagement.jira.plugin.customfields.CustomFieldService;
import com.transcendmanagement.jira.plugin.screens.ScreenSchemeService;
import com.transcendmanagement.jira.plugin.workflows.WorkflowService;

public class MyAddProjectHook extends EmptyAddProjectHook {

	private final CustomFieldService customFieldService;
	private final ScreenSchemeService screenSchemeService;
	private final WorkflowService workflowService;
	
	private static final Logger log = LoggerFactory
			.getLogger(MyAddProjectHook.class);

	public MyAddProjectHook(CustomFieldService customFieldService,
			ScreenSchemeService screenSchemeService,
			WorkflowService workflowService) {
		this.customFieldService = customFieldService;
		this.screenSchemeService = screenSchemeService;
		this.workflowService = workflowService;
	}

	@Override
	public ValidateResponse validate(final ValidateData validateData) {
		ValidateResponse validateResponse = ValidateResponse.create();
		/*
		 * if (validateData.projectKey().equals("TEST")){
		 * validateResponse.addErrorMessage("Invalid Project Key"); }
		 */		
		return validateResponse;
	}

	@Override
	public ConfigureResponse configure(final ConfigureData configureData) {
		
		customFieldService.createAllCustomFields(configureData.project());
		screenSchemeService.createScreenSchemes(configureData.project());
		workflowService.createAllWorkFlows(configureData.project());
		
		ConfigureResponse configureResponse = ConfigureResponse.create()
				.setRedirect("/issues/");
		return configureResponse;
	}

}