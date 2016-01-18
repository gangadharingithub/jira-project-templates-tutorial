package com.transcendmanagement.jira.plugin.workflows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;

@Component
public class WorkflowService {
	
	private final WorkflowManager workflowManager;
	private final WorkflowSchemeManager workflowSchemeManager;
	private final IssueTypeManager issueTypeManager;
	
	private static final Logger LOG = Logger.getLogger(WorkflowService.class);
	
	public WorkflowService(WorkflowManager workflowManager,
			WorkflowSchemeManager workflowSchemeManager,IssueTypeManager issueTypeManager){		
		this.workflowManager=workflowManager;		
		this.workflowSchemeManager=workflowSchemeManager;
		this.issueTypeManager=issueTypeManager;
	}
	
	private String getIssueTypeId(String issueTypeName){
		
		IssueType issueType = Iterables.find(issueTypeManager.getIssueTypes(), new Predicate<IssueType>(){

			@Override
			public boolean apply(IssueType input) {
				return input.getName().equalsIgnoreCase("Article");
			}
			
		},null);
		
		if(issueType !=null){
			return issueType.getId();			
		}else{
			// create issue type.
			return issueTypeManager.createIssueType("Article", "Article",0l).getId();
			
		}
	}
	
	
	public void createAllWorkFlows(Project Project){
		
		Scheme scheme;
		
		//TODO  DEV
		/*if(workflowSchemeManager.getWorkflowSchemeObj("TMax Launch Workflow Scheme")!=null){
			
			try {
				workflowSchemeManager.deleteScheme(workflowSchemeManager.getWorkflowSchemeObj("TMax Launch Workflow Scheme").getId());
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/
		
		if(workflowSchemeManager.getWorkflowSchemeObj("TMax Launch Workflow Scheme")==null) {
			
			scheme = workflowSchemeManager.createSchemeObject("TMax Launch Workflow Scheme","TMax Launch Workflow Scheme");
		}else{
			
			scheme = workflowSchemeManager.getSchemeObject("TMax Launch Workflow Scheme");
		}
		
			
		try {			
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Article Tracking","Article Tracking",
					"/wfb/Article.xml",Lists.newArrayList(getIssueTypeId("Article")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"BMC 1.0","BMC 1.0",
					"/wfb/BMC.xml",Lists.newArrayList(getIssueTypeId("BMC Assumption")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Buffer Workflow","Buffer Workflow",
					"/wfb/Buffer.xml",Lists.newArrayList(getIssueTypeId("Buffer")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Bug Workflow","Bug Workflow",
					"/wfb/Bug.xml",Lists.newArrayList(getIssueTypeId("Bug")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Change Management Workflow","Change Management Workflow",
					"/wfb/Change.xml",Lists.newArrayList(getIssueTypeId("Epic")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Issue Workflow","Issue Workflow",
					"/wfb/Issue.xml",Lists.newArrayList(getIssueTypeId("Deficiency")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Requirement 1.0","Requirement 1.0",
					"/wfb/Requirement.xml",Lists.newArrayList(getIssueTypeId("Requirement")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Software Task 1.0","Software Task 1.0",
					"/wfb/Software.xml",Lists.newArrayList(getIssueTypeId("New Feature"),getIssueTypeId("Improvement")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Task workflow 1.0","Task workflow 1.0",
					"/wfb/Task.xml",Lists.newArrayList(getIssueTypeId("Task")));
			
			createWorkflowFromXML(workflowSchemeManager.getScheme(scheme.getId()),
					"Testing Workflow 1.0","Testing Workflow 1.0",
					"/wfb/Testing.xml",Lists.newArrayList(getIssueTypeId("Test")));
			
			
		} catch (GenericEntityException e) {
			Log.error("workflow Scheme  already exists with the name: TMax Launch Workflow Scheme");
		}
		
		
	}
	
	
	private void createWorkflowFromXML(GenericValue schemeName,String name,String desc,String filePath,List<String> issueTypeIds){
		
		try {
			
			if(workflowManager.getWorkflowFromScheme(schemeName, name)!=null){				
				LOG.warn("workflow already exists with the name :" + name);
				return;				
			}
			
			//final WorkflowDescriptor workflowDescriptor = WorkflowLoader.load(new FileInputStream("test.xml"), true);
			final WorkflowDescriptor workflowDescriptor = WorkflowLoader.load(
					ClassLoaderUtils.getResourceAsStream(filePath,this.getClass()), true);
							
			final ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(name, workflowDescriptor, workflowManager);			
			newWorkflow.setDescription(desc);			
			workflowManager.createWorkflow(ComponentAccessor.getJiraAuthenticationContext().getUser(), newWorkflow);
			for(String issueTypeId : issueTypeIds){
				workflowSchemeManager.addWorkflowToScheme(schemeName,newWorkflow.getName(),issueTypeId);
			}
			
		} catch (InvalidWorkflowDescriptorException e) {
			LOG.error("error while creating workflow :" + e);
		} catch (FileNotFoundException e) {
			LOG.error("error while creating workflow :" + e);
		} catch (SAXException e) {
			LOG.error("error while creating workflow :" + e);
		} catch (IOException e) {
			LOG.error("error while creating workflow :" + e);
		} catch (GenericEntityException e) {
			LOG.error("error while creating workflow :" + e);
		}
		
	}

}
