package com.transcendmanagement.jira.plugin.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.version.VersionManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
public class SetUpIssueStatusChartService {
	
	private static final Logger Log = LoggerFactory
			.getLogger(SetUpIssueStatusChartService.class);
	
	private final IssueManager issueManager;
	private final CustomFieldManager customFieldManager;
	private final IssueTypeManager issueTypeManager;
	private final FieldScreenManager fieldScreenManager;	
	private final VersionManager versionManager;

	final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private final  List<JiraContextNode> contexts;
	
	final List<String> issueTypes = Lists.newArrayList("Problem","Bus Ops","Field Issue","Bug","Deficiency");
	final List<String> cfNames = Lists.newArrayList("Planned Completion","Committed Completion");
	private final CustomFieldType  dateType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:datepicker");
	private final CustomFieldSearcher dateSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:datesearcher");
	
	public SetUpIssueStatusChartService(IssueManager IssueManager,
			CustomFieldManager customFieldManager,
			IssueTypeManager issueTypeManager,
			FieldScreenManager fieldScreenManager,
			VersionManager versionManager){
		this.issueManager=IssueManager;
		this.customFieldManager=customFieldManager;
		this.issueTypeManager = issueTypeManager;	
		contexts = Lists.newArrayList(GlobalIssueContext.getInstance());
		this.fieldScreenManager=fieldScreenManager;
		this.versionManager=versionManager;
	}
	
	public  boolean setupData() throws CreateException{
		
		//versions
		Date startDate = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay().toDate();
		Date releaseDate = new DateTime().dayOfMonth().withMaximumValue().withTimeAtStartOfDay().toDate();
		createVersion("v1",startDate,releaseDate);
		
		//issue Types ==> Problem, Bus Ops,Field Issue,Bug,Deficiency == > MileStone 
		for(String issueType: issueTypes){
			createIssueType(issueType);
		}
		//status ==> new, In Progress,Closed
		
		//custom fields ==>  "planned completion" and "Committed Completion"
		List<String> screens = Lists.newArrayList("Default Screen");
		for(String cfName: cfNames){
			createCustomField(cfName, cfName, dateType, dateSearcher, contexts, Lists.newArrayList("MileStone"), null, screens);
		}
		
		return true;	
		
	}
	
	private void createVersion(String version,Date startDate,Date releaseDate) throws CreateException{		
		
		//createVersion(String name, Date startDate, Date releaseDate, String description, Long projectId, Long scheduleAfterVersion) 
		if(versionManager.getVersionsByName(version).isEmpty()){
			versionManager.createVersion(version, startDate, releaseDate, version, 10000l, null);
		}
		
	}
	
	
	private void createIssueType(final String issueTypeName){
		
		IssueType issueType = Iterables.find(issueTypeManager.getIssueTypes(), new Predicate<IssueType>(){

			@Override
			public boolean apply(IssueType input) {
				return input.getName().equalsIgnoreCase(issueTypeName);
			}
			
		},null);
		
		if(issueType ==null){		
			issueTypeManager.createIssueType(issueTypeName, issueTypeName,0l);			
		}
	}	
	
	private void createCustomField(String name,String desc,CustomFieldType type,
			CustomFieldSearcher searcher,List<JiraContextNode> context,
			final List<String> issueTypes,List<String> options,List<String> screens){
			
			List<GenericValue>  issueTypeValues = this.getIssueTypes(issueTypes);			
			
		try {
			
			CustomField customField = customFieldManager.createCustomField(name, 
					desc,type, searcher, 
					contexts,issueTypeValues);
			
			if(customField!=null){
				
				/*if(type instanceof SelectCFType && options!=null){
					addOptionToCustomField(customField,options);
				}
				*/
				associateToScreens (customField,screens);	
				
			}else{
				throw new GenericEntityException();
			}
			
		} catch (GenericEntityException e) {
			//LOG.error("error while creating custom field : " + name);
		}
		
	}
	
	private void associateToScreens(CustomField  field,List<String> screenNames){
		
		for(final String screenName : screenNames){
			
			FieldScreen screen = Iterables.find(fieldScreenManager.getFieldScreens(),
					new Predicate<FieldScreen>() {
						@Override
						public boolean apply(FieldScreen paramT) {
							 return paramT.getName().equalsIgnoreCase(screenName);
						}
					},null);
			
			 if (screen!=null && !screen.containsField(field.getId())) {
				FieldScreenTab firstTab = screen.getTab(0);
				firstTab.addFieldScreenLayoutItem(field.getId());
			 }
			
		}
		
	}
	
	private List<GenericValue>  getIssueTypes(final List<String>  issueTypeNames){
		
		List<IssueType>  issueTypeObjs = Lists.newArrayList();
		
		issueTypeObjs = Lists.newArrayList(Collections2.filter(issueTypeManager.getIssueTypes(),
    			new Predicate<IssueType>() {
		            public boolean apply(IssueType issueType) {
		            	return issueTypeNames.contains(issueType.getName());
		            }
    			}
    	));
		
		issueTypeObjs = Lists.newArrayList(Iterables.filter(issueTypeObjs,Predicates.notNull()));
		
		return Lists.transform(issueTypeObjs,new Function<IssueType, GenericValue>() {
						@Override
						public GenericValue apply(final IssueType issueType) {
							return issueType.getGenericValue();
									
						}
					}
				);
		
	}
	
}
