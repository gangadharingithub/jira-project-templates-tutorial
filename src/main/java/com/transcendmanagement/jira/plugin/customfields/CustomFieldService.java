package com.transcendmanagement.jira.plugin.customfields;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.stereotype.Component;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
public class CustomFieldService {
	
	private final CustomFieldManager customFieldManager;
	private final IssueTypeManager issueTypeManager;
	private final OptionsManager optionsManager;
	
	private CustomFieldSearcher fieldTypeSearcher;
	private  List<JiraContextNode> contexts;
	private final FieldScreenManager fieldScreenManager;
	
	private CustomFieldType textFieldType,selectFieldType,textAreaType,dateType,multiSelectType,numberType,checkboxType;
	private CustomFieldSearcher textFieldSearcher,textAreaSearcher,dateSearcher,multiSelectSearcher,numberSearcher,checkboxSearcher;
	
	private static final Logger LOG = Logger.getLogger(CustomFieldService.class);
	
	CustomFieldService(CustomFieldManager customFieldManager,
			IssueTypeManager issueTypeManager,
			OptionsManager optionsManager,
			 FieldScreenManager fieldScreenManager){
		this.customFieldManager=customFieldManager;
		this.issueTypeManager=issueTypeManager;
		this.optionsManager=optionsManager;
		this.fieldScreenManager=fieldScreenManager;
		
		// lloks like we need to load these in a lazy manner.
		
		/*selectFieldType = 
    			this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:select");
		
        fieldTypeSearcher = 
        		this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:selectsearcher");
        
         textFieldType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:textfield");        
         textFieldSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher");
         
         textAreaType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:textarea");        
         textAreaSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher");
         
         multiSelectType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:multiselect");        
         multiSelectSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:multiselectsearcher");
         
         dateType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:datepicker");        
         dateSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:datesearcher");
         
         numberType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:float");
         numberSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:exactnumber");
         
         checkboxType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes");
         checkboxSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:checkboxsearcher"); 
        
         contexts = Lists.newArrayList(GlobalIssueContext.getInstance());
        */
	}
	
	public void createAllCustomFields(Project project){
		
		selectFieldType = 
    			this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:select");
		
        fieldTypeSearcher = 
        		this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:selectsearcher");
        
         textFieldType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:textfield");        
         textFieldSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher");
         
         textAreaType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:textarea");        
         textAreaSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher");
         
         multiSelectType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:multiselect");        
         multiSelectSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:multiselectsearcher");
         
         dateType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:datepicker");        
         dateSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:datesearcher");
         
         numberType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:float");
         numberSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:exactnumber");
         
         checkboxType = this.customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes");
         checkboxSearcher = this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:checkboxsearcher"); 
        
         //TODO  how about project and isse
         //ProjectContext projectContext = new ProjectContext(project.getId());         
         contexts = Lists.newArrayList(GlobalIssueContext.getInstance());
         
		//Single select list-- -Start 
		
		createCustomField(
				"BMC Section",
				"Business Model Canvas Section",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("BMC Assumption"),
				Lists.newArrayList("Channels",
						"Customer Relationship",
						"Customer Segments",
						"Investment / Inventory",
						"Key Activities",
						"Key Metrics",
						"Key Partners",
						"Key Resources",
						"Operating Expense",
						"Problem",
						"Solution",
						"Throughput",
						"Unfair Advantage",
						"Value Proposition"),
				Lists.newArrayList("BMC Assumption","BMC Create"));
		
		createCustomField(
				"Change Type",
				"Change Type",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("Epic"),
				Lists.newArrayList("Type 1 - Significant",
						"Type 2 - Major",
						"Type 3 - Minor"),
				Lists.newArrayList("Change Request"));
		
		createCustomField(
				"Creation Type",
				"How was the creation of this item referenced. New item with no previous item. An identical copy of another item or a modification of another item",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("Requirement"),
				Lists.newArrayList("New",
						"Identical",
						"Modified"),
				Lists.newArrayList("Requirement Create","Requirement"));
		
		createCustomField(
				"Eligible",
				"Eligible",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("Test","sub test","Task","sub task","Milestone","Improvement","New Feature"),
				Lists.newArrayList("Yes",
						"No"),
				Lists.newArrayList("Milestone Screen","Task Screen","Test Screen"));
		
		createCustomField(
				"Requirement Level",
				"Requirement Level",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("Requirement"),
				Lists.newArrayList("Market","Product",
						"Sub-system"),
				Lists.newArrayList("Requirement","Requirement Create"));
		
		createCustomField(
				"Requirement Type",
				"Requirement Type",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("Test","sub Test"),
				Lists.newArrayList("Development","Production",
						"Validation","Verification"),
				Lists.newArrayList("Test Create Screen","Test Edit Screen","Test Screen"));
		
		createCustomField(
				"Test Type",
				"Type of Test Being Performed",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("Requirement"),
				Lists.newArrayList("Requirement","Goal",
						"Option"),
				Lists.newArrayList("Requirement","Requirement Create"));
		
		createCustomField(
				"Validation Setup",
				"Test setup or method used to validate requirement",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("Requirement","sub test","Test"),
				Lists.newArrayList("tbd1","tbd2",
						"tbd3"),
				Lists.newArrayList("Requirement","Requirement Create","Test Create Screen","Test Edit Screen","Test Screen"));
		
		createCustomField(
				"Verification Setup",
				"Test setup or method used to verify requirement",
				selectFieldType,
				fieldTypeSearcher,
				contexts,
				Lists.newArrayList("Requirement","sub test","Test"),
				Lists.newArrayList("tbd1","tbd2",
						"tbd3"),
				Lists.newArrayList("Requirement","Requirement Create","Test Create Screen","Test Edit Screen","Test Screen"));
		
		//Single Select list--  End
		
		//textarea-  start
		
		createCustomField(
				"Corrective Action",
				"What actions are needed to fix failure or issue?",
				textFieldType,
				textFieldSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Bug","Issue"),
				null,
				Lists.newArrayList("BMC Assumption","BMC Create"));
		
		
		createCustomField(
				"Implementation Plan",
				"Describe how this will be Implemented",
				textAreaType,
				textAreaSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Epic"),
				null,
				Lists.newArrayList("Change Screen"));
		
		createCustomField(
				"Modifications",
				"Design or system modifications that need to be implemented.",
				textAreaType,
				textAreaSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Epic","Bug","Issue"),
				null,
				Lists.newArrayList("Change Screen","Issue Screen"));
		
		createCustomField(
				"Rationale",
				"Describe the reasoning behind the requirement or change",
				textAreaType,
				textAreaSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement"),
				null,
				Lists.newArrayList("Change Request","Requirement","Requirement Create"));
		
		createCustomField(
				"Root Cause",
				"Root Cause",
				textAreaType,
				textAreaSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Issue","Bug"),
				null,
				Lists.newArrayList("Issue Create Screen","Issue Screen"));
		
		createCustomField(
				"Serial Number",
				"Serial Number",
				textAreaType,
				textAreaSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Article"),
				null,
				Lists.newArrayList("Article Screen"));
		
		createCustomField(
				"Test Method",
				"Describe how the Requirement will be tested.",
				textAreaType,
				textAreaSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement"),
				null,
				Lists.newArrayList("Requirement","Requirement Create"));
		
		createCustomField(
				"Test Results",
				"Results of testing being conducted.",
				textAreaType,
				textAreaSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Test","sub Test"),
				null,
				Lists.newArrayList("Test Edit Screen","Test Screen"));		
		//textarea--  end
		
		//Multi Select list--  start
		
		createCustomField(
				"Validation Setup",
				"Test setup or method used to validate requirement",
				multiSelectType,
				multiSelectSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement","Test","sub test"),
				Collections.<String>emptyList(),
				Lists.newArrayList("Requirement","Requirement Create","Test Create Screen","Test Edit Screen","Test Screen"));
		
		createCustomField(
				"Verification Setup",
				"Test setup or method used to verify requirement",
				multiSelectType,
				multiSelectSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement","Test","sub test"),
				Collections.<String>emptyList(),
				Lists.newArrayList("Requirement","Requirement Create","Test Create Screen","Test Edit Screen","Test Screen"));
		
		
		//Multi Select list--  end
		
		//number --  start
		
		createCustomField(
				"Completion Probability",
				"Probability milestone will complete prior to commitment date",
				numberType,
				numberSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Test","sub test","Task","sub task","Milestone","Improvement","New Feature"),
				null,
				Lists.newArrayList("Buffer Screen","Milestone Screen","Task Create Screen",
						"Task Edit Screen","Test Create Screen","Test Edit Screen","Test Screen"));
		
			
		//number --  end
		
		//checkbox --  start
		
		createCustomField(
				"BMC Validation Method",
				"Method used to validate Business Model Canvas Assumption",
				checkboxType,
				checkboxSearcher,
				contexts,
				//issue types
				Lists.newArrayList("BMC Assumption"),
				Lists.newArrayList("Market Research","Customer Inverviews","Analysis"),
				Lists.newArrayList("BMC Assumption","BMC Create"));
		
		//checkbox --  end
		
		//date  --start
		
		createCustomField(
				"Committed Completion",
				"Results of testing being conducted.",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Test","sub test","Task","sub task","Milestone","Improvement","New Feature"),
				null,
				Lists.newArrayList("Buffer Screen","Milestone Screen","Task Create Screen",
						"Task Edit Screen","Test Create Screen","Test Edit Screen","Test Screen"));
		
		
		createCustomField(
				"Implemented Date",
				"Date Issue was implemented.",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement"),
				null,
				Lists.newArrayList("Requirement(Dates)"));
		
		createCustomField(
				"Implementation Planned Date",
				"Date that Implementation is planned to be completed",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement"),
				null,
				Lists.newArrayList("Requirement(Dates)"));
		
		createCustomField(
				"Planned Completion",
				"Fully Buffered Date",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Collections.<String>emptyList(),
				null,
				Lists.newArrayList("Milestone Screen","Task Screen","Test Screen"));
		
		createCustomField(
				"Projected Finish",
				"Fully Buffered Date",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Test","sub test","Task","sub task","Milestone","Improvement","New Feature"),
				null,
				Lists.newArrayList("Buffer Screen","Milestone Screen","Task Create Screen",
						"Task Edit Screen","Test Create Screen","Test Edit Screen","Test Screen"));
		
		createCustomField(
				"Projected Start",
				"Fully Buffered Date",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Test","sub test","Task","sub task","Milestone","Improvement","New Feature"),
				null,
				Lists.newArrayList("Buffer Screen","Milestone Screen","Task Create Screen",
						"Task Edit Screen","Test Create Screen","Test Edit Screen","Test Screen"));
		
		createCustomField(
				"Status Date",
				"Date that schedule was last updated",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Buffer","Milestone"),
				null,
				Lists.newArrayList("Buffer Screen","Milestone Screen"));
		
		createCustomField(
				"Validated Date",
				"Validated Date",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement"),
				null,
				Lists.newArrayList("Requirement(Dates)"));
		
		createCustomField(
				"Validation Planned Date",
				"Validation Planned Date",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement"),
				null,
				Lists.newArrayList("Requirement(Dates)"));
		
		createCustomField(
				"Verified Date",
				"Date issue was verified",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement"),
				null,
				Lists.newArrayList("Requirement(Dates)"));
		
		createCustomField(
				"Verification Planned Date",
				"Validation Planned Date",
				dateType,
				dateSearcher,
				contexts,
				//issue types
				Lists.newArrayList("Requirement"),
				null,
				Lists.newArrayList("Requirement(Dates)"));
		
		//date  --end
		
	
	}
	
	
	private void createCustomField(String name,String desc,CustomFieldType type,
			CustomFieldSearcher searcher,List<JiraContextNode> context,
			final List<String> issueTypes,List<String> options,List<String> screens){
			
			List<GenericValue>  issueTypeValues = this.getIssueTypes(issueTypes);			
			//cleanup(name);
			
		try {
			
			if(customFieldManager.getCustomFieldObjectByName(name)==null){
			
				CustomField customField = customFieldManager.createCustomField(name, 
						desc,type, searcher, 
						contexts,issueTypeValues);
				
				if(customField!=null){
					
					if(type instanceof SelectCFType && options!=null){
						addOptionToCustomField(customField,options);
					}
					
					associateToScreens (customField,screens);	
					
				}else{
					throw new GenericEntityException("Not able to  create custom field with the name :" + name);
				}
			}else{
				LOG.error("custom field already exists : " + name);
			}
			
		} catch (GenericEntityException e) {
			LOG.error("error while creating custom field : " + name);
		}
		
	}
	
	//DEV  TODO: remove at the end
	/*private void cleanup(String name){
		
		while(customFieldManager.getCustomFieldObjectByName(name)!=null){
			
			try {
				customFieldManager.removeCustomField(customFieldManager.getCustomFieldObjectByName(name));
			} catch (RemoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}*/
	//DEV  TODO: remove at the end	
	
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
	
	private void addOptionToCustomField(CustomField customField, List<String> values) {
		
	    if (customField != null) {
	        List<FieldConfigScheme> schemes = customField
	                .getConfigurationSchemes();
	        if (schemes != null && !schemes.isEmpty()) {
	            FieldConfigScheme sc = schemes.get(0);
	            Map configs = sc.getConfigsByConfig();
	            if (configs != null && !configs.isEmpty()) {
	                FieldConfig config = (FieldConfig) configs.keySet()
	                        .iterator().next();	 
	                for(int i = 0;i< values.size();i++){
	                	optionsManager.createOption(config, null,
		                        new Long(i), values.get(i));
	                }
	                
	            }
	        }
	    }
		
	}
	
}


