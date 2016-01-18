package com.transcendmanagement.jira.plugin.screens;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.stereotype.Component;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
public class ScreenSchemeService {
	
	
	private final FieldScreenSchemeManager fieldScreenSchemeManager;
	private final FieldScreenManager fieldScreenManager;
	private final FieldManager fieldManager;
	private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
	private final ConstantsManager constantsManager;
	private final IssueTypeManager  issueTypeManger;
	
	
	private static final Logger LOG = Logger.getLogger(ScreenSchemeService.class);
	
	
	public ScreenSchemeService(FieldScreenSchemeManager fieldScreenSchemeManager,
			FieldScreenManager fieldScreenManager,
			FieldManager fieldManager,IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
			ConstantsManager constantsManager,IssueTypeManager  issueTypeManager){		
		this.fieldScreenSchemeManager =fieldScreenSchemeManager;
		this.fieldScreenManager =fieldScreenManager;
		this.fieldManager=fieldManager;	
		this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
		this.constantsManager=constantsManager;
		this.issueTypeManger=issueTypeManager;
	}
	
	private IssueType getIssueTypeObj(String name){
		return issueTypeManger.getIssueType(name);
	}
	
	private void associateIssueTypeScreenScheme(Project project,final IssueType issueType,FieldScreenScheme fieldScreenScheme,
			IssueTypeScreenScheme myIssueTypeScreenScheme){
		
		if(issueType!=null && fieldScreenScheme !=null && myIssueTypeScreenScheme!=null){	
			
			IssueTypeScreenSchemeEntity myEntity = Iterables.find(myIssueTypeScreenScheme.getEntities(),
					new Predicate<IssueTypeScreenSchemeEntity>() {
			            public boolean apply(IssueTypeScreenSchemeEntity myEntity) {
			            	return myEntity.getIssueTypeObject().getName().equalsIgnoreCase(issueType.getName());
			            }
					},null
				);
			
			
			if(myEntity==null){
				
				myEntity = new IssueTypeScreenSchemeEntityImpl(
				        issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, constantsManager);			
				myEntity.setIssueTypeId(issueType != null ? issueType.getId() : null); // an entity can be for all IssueTypes (-> null), or just for 1
				myEntity.setFieldScreenScheme(fieldScreenScheme);
				myIssueTypeScreenScheme.addEntity(myEntity);	
				
			}else{
				
				LOG.warn("IssueTypeScreenSchemeEntityexists for this issue type :" + issueType.getName()+ ":" + fieldScreenScheme.getName());
			}
						
			// assign to project
			if(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project)==null){
				issueTypeScreenSchemeManager.addSchemeAssociation(project,myIssueTypeScreenScheme);
			}else{
				LOG.warn("IssueTypeScreenScheme already associated to this project :" + myIssueTypeScreenScheme.getName() + ":" + project.getName());
			}
			
		}
		
	}
	
	
	public void createScreenSchemes(Project project){
		
		IssueTypeScreenScheme myIssueTypeScreenScheme = Iterables.find(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes(),
				new Predicate<IssueTypeScreenScheme>() {
		            public boolean apply(IssueTypeScreenScheme issueTypeScreenScheme) {
		            	return issueTypeScreenScheme.getName().equalsIgnoreCase("TMax Launch Issue Type Screen Scheme");
		            }
				},null
			);
		
		if(myIssueTypeScreenScheme==null) {
			
			myIssueTypeScreenScheme = new IssueTypeScreenSchemeImpl(issueTypeScreenSchemeManager, null);
			myIssueTypeScreenScheme.setName("TMax Launch Issue Type Screen Scheme");
			myIssueTypeScreenScheme.setDescription("TMax Launch Issue Type Screen Scheme");
			myIssueTypeScreenScheme.store();
			
		}
		
		/// =================  Article Screen =====================
		FieldScreen fieldScreen1  = createScreen(
					"Article Screen",
					"Article Screen",
					Lists.newArrayList("Summary","Description","Product","Serial Number","Epic Link","Labels","Resolution Color"));
		
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Article"), 
				createScreenScheme("Article Screen Scheme","Article Screen Scheme",fieldScreen1,null), 
				myIssueTypeScreenScheme);	
		
		//========================  BMC Assumption ============================
		
		FieldScreen fieldScreen2  = createScreen(
					"BMC Assumption",
					"BMC Assumption",
					Lists.newArrayList("Summary","Description","BMC Section",
							"Product","Fix Version/s","Components","Priority",
							"Epic Link","BMC Validation Method","Resolution Color"));
		//createScreenScheme("BMC Screen Scheme","BMC Screen Scheme",fieldScreen2,null);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("BMC Assumption"), 
				createScreenScheme("BMC Screen Scheme","BMC Screen Scheme",fieldScreen2,null), 
				myIssueTypeScreenScheme);
		
		FieldScreen fieldScreen3  = createScreen(
				"BMC Assumption Create",
				"BMC Assumption Create",
				Lists.newArrayList("Summary","Description","BMC Section",
						"Product","Priority","Epic Link","BMC Validation Method","Resolution Color"));
		//createScreenScheme("BMC Screen Scheme",fieldScreen3, IssueOperations.CREATE_ISSUE_OPERATION);		
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("BMC Assumption"), 
				createScreenScheme("BMC Screen Scheme","BMC Screen Scheme",fieldScreen3, IssueOperations.CREATE_ISSUE_OPERATION), 
				myIssueTypeScreenScheme);
		
		//======================== Buffer Screen ============================		
																													
		FieldScreen fieldScreen4  = createScreen(
				"Buffer Screen",
				"Buffer Screen",
				Lists.newArrayList("Summary","Description","Completion Probability",
						"Due Date","Projected Start","Committed Completion","Status Date",
						"Resolution Color"));
		//createScreenScheme("Buffer Screen Scheme","Buffer Screen Scheme",fieldScreen4,null);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Buffer"), 
				createScreenScheme("Buffer Screen Scheme","Buffer Screen Scheme",fieldScreen4,null), 
				myIssueTypeScreenScheme);
		
		//======================== Change Request ============================
		
		FieldScreen fieldScreen5  = createScreen(
				"Change Request",
				"Change Request",
				Lists.newArrayList("Summary","Description","Change Type","Rationale",
						"Modifications","Labels","Team","Fix Versions","Product",
						"Affects Versions","Epic Name","Epic Color","Resolution Color"));
		//createScreenScheme("Innovation Screen Scheme","Innovation Screen Scheme",fieldScreen5,null);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Innovation"), 
				createScreenScheme("Innovation Screen Scheme","Innovation Screen Scheme",fieldScreen5,null), 
				myIssueTypeScreenScheme);
		
		//======================== Issue Screen ============================
		//TODO  issue type ? assuming Story
		FieldScreen fieldScreen6  = createScreen(
				"Issue Screen",
				"Issue Screen",
				Lists.newArrayList("Summary","Description","Product","Priority","Epic Link","Linked Issues",
						"Root Cause","Corrective Action","Modifications","Affects Version","Fix Versions","Components","Resolution Color"
						));
		//createScreenScheme("Issue Screen Scheme","Issue Screen Scheme",fieldScreen6,null);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Story"), 
				createScreenScheme("Issue Screen Scheme","Issue Screen Scheme",fieldScreen6,null), 
				myIssueTypeScreenScheme);
		
		FieldScreen fieldScreen7  = createScreen(
				"Issue Create Screen",
				"Issue Create Screen",
				Lists.newArrayList("Summary","Description","Product","Priority","Epic Link","Linked Issues",
						"Root Cause","Affects Version"));
		//associateScreenToScheme("Issue Screen Scheme",fieldScreen7, IssueOperations.CREATE_ISSUE_OPERATION);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Story"), 
				createScreenScheme("Issue Screen Scheme","Issue Screen Scheme",fieldScreen7, IssueOperations.CREATE_ISSUE_OPERATION), 
				myIssueTypeScreenScheme);
		
		//======================== Milestone ============================
		
		FieldScreen fieldScreen8  = createScreen(
				"Milestone Screen",
				"Milestone Screen",
				Lists.newArrayList("Summary","Description","Product","Priority","Fix Versions","Components",
						"Epic Link","Sprint","Labels","Predecessor","Successor","Completion Probability","Eligible",
						"MSP UID","Resolution Color","Status Date","Projected Start","Projected Finish","Committed Completion","Due Date"));
		//createScreenScheme("Milestone","Milestone",fieldScreen8,null);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Milestone"), 
				createScreenScheme("Milestone","Milestone",fieldScreen8,null), 
				myIssueTypeScreenScheme);
		
		//======================== Requirement ============================
		
		FieldScreen fieldScreen9  = createScreen(
				"Requirement",
				"Requirement",
				Lists.newArrayList("Summary","Test Type","Description","Verification Setup","Validation Setup","Priority",
						"Product","Fix Versions","Components","Test Results","Epic Link","Time Tracking","Sprint",
						"Labels","Predecessor",	"Successor","Requirements","Completion Probability","Eligible",
						"MSP UID","Resolution Color","Due Date","Projected Start","Projected Finish","Committed Completion"));
		//createScreenScheme("Requirement Screen Scheme","Requirement Screen Scheme",fieldScreen9,null);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Requirement"), 
				createScreenScheme("Requirement Screen Scheme","Requirement Screen Scheme",fieldScreen9,null), 
				myIssueTypeScreenScheme);
		
		
		FieldScreen fieldScreen11  = createScreen(
				"Requirement Create",
				"Requirement Create",
				Collections.<String>emptyList());
		//associateScreenToScheme("Requirement Screen Scheme",fieldScreen11, IssueOperations.CREATE_ISSUE_OPERATION);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Requirement"), 
				createScreenScheme("Requirement Screen Scheme","Requirement Screen Scheme",fieldScreen11, IssueOperations.CREATE_ISSUE_OPERATION), 
				myIssueTypeScreenScheme);
		
		//======================== Task Screen ============================
		
		FieldScreen fieldScreen12  = createScreen(
				"Task Screen",
				"Task Screen",
				Lists.newArrayList("Summary","Description",	"Priority","Product","Fix Versions","Components","Epic Link","Time Tracking",
						"Sprint","Labels","Requirements","Predecessor","Successor","Eligible","MSP UID","Completion Probability","Resolution Color",
						"Due Date","Projected Start","Projected Finish","Committed Completion"));
		//createScreenScheme("Task Screen Scheme","Task Screen Scheme",fieldScreen12,null);		
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Task"), 
				createScreenScheme("Task Screen Scheme","Task Screen Scheme",fieldScreen12,null), 
				myIssueTypeScreenScheme);
		
		FieldScreen fieldScreen13  = createScreen(
				"Task Create Screen",
				"Task Create Screen",
				Lists.newArrayList("Summary","Description","Priority","Product","Fix Versions",
						"Components","Epic Link","Sprint","Labels","Committed Completion"));
		//associateScreenToScheme("Task Screen Scheme",fieldScreen13, IssueOperations.CREATE_ISSUE_OPERATION);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Task"), 
				createScreenScheme("Task Screen Scheme","Task Screen Scheme",fieldScreen13, IssueOperations.CREATE_ISSUE_OPERATION), 
				myIssueTypeScreenScheme);
		
		FieldScreen fieldScreen14  = createScreen(
				"Task Edit Screen",
				"Task Edit Screen",
				Lists.newArrayList("Summary","Description","Priority","Product","Fix Versions","Components","Epic Link","Time Tracking","Sprint",
						"Labels","Completion Probability","Eligible","Resolution Color","Due Date",	"Projected Start","Projected Finish","Committed Completion"));
		//associateScreenToScheme("Task Screen Scheme",fieldScreen14, IssueOperations.EDIT_ISSUE_OPERATION);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Task"), 
				createScreenScheme("Task Screen Scheme","Task Screen Scheme",fieldScreen14, IssueOperations.EDIT_ISSUE_OPERATION), 
				myIssueTypeScreenScheme);	
		
		//======================== Test Screen ============================
		
		FieldScreen fieldScreen15  = createScreen(
				"Test Screen",
				"Test Screen",
				Lists.newArrayList("Summary","Test Type","Description","Verification Setup","Validation Setup","Priority","Product",
						"Fix Versions","Components","Test Results","Epic Link","Time Tracking","Sprint","Labels","Predecessor",
						"Successor","Requirements","Completion Probability","Eligible","MSP UID","Resolution Color","Due Date",
						"Projected Start","Projected Finish","Committed Completion"));
		//createScreenScheme("Test Screen Scheme","Test Screen Scheme",fieldScreen15, null);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Test"), 
				createScreenScheme("Test Screen Scheme","Test Screen Scheme",fieldScreen15, null), 
				myIssueTypeScreenScheme);
		
		
		FieldScreen fieldScreen16  = createScreen(
				"Test Create Screen",
				"Test Create Screen",
				Lists.newArrayList("Summary","Test Type","Description","Verification Setup","Validation Setup","Priority",
						"Product","Fix Versions","Components","Labels","Committed Completion"));
		//associateScreenToScheme("Test Screen Scheme",fieldScreen16, IssueOperations.CREATE_ISSUE_OPERATION);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Test"), 
				createScreenScheme("Test Screen Scheme","Test Screen Scheme",fieldScreen16, IssueOperations.CREATE_ISSUE_OPERATION), 
				myIssueTypeScreenScheme);
		
		
		FieldScreen fieldScreen17  = createScreen(
				"Test Edit Screen",
				"Test Edit Screen",
				Lists.newArrayList("Summary","Test Type","Description","Verification Setup","Validation Setup","Priority","Product","Fix Versions",
						"Components","Test Results","Epic Link","Time Tracking","Sprint","Labels","Completion Probability","Due Date","Projected Start",
						"Projected Finish",	"Committed Completion"));
		//associateScreenToScheme("Test Screen Scheme",fieldScreen17, IssueOperations.CREATE_ISSUE_OPERATION);
		this.associateIssueTypeScreenScheme(project, this.getIssueTypeObj("Test"), 
				createScreenScheme("Test Screen Scheme","Test Screen Scheme",fieldScreen17, IssueOperations.EDIT_ISSUE_OPERATION), 
				myIssueTypeScreenScheme);
		
		
		
		
	}
	
	/*private void associateScreenToScheme(final String name,FieldScreen fieldScreen,
			ScreenableIssueOperation issueOperation){
		

		if(fieldScreen ==null) {
			LOG.error("not able to create screen");
			return;			
		}
		
		
		FieldScreenScheme fieldScreenScheme ; 
		
		fieldScreenScheme = Iterables.find(fieldScreenSchemeManager.getFieldScreenSchemes(),
			new Predicate<FieldScreenScheme>() {
	            public boolean apply(FieldScreenScheme screenScheme) {
	            	return screenScheme.getName().equalsIgnoreCase(name);
	            }
			},null
		);
		
		if(fieldScreenScheme!=null){			
			FieldScreenSchemeItem mySchemeItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
			//wanted to make this default
			if(issueOperation!=null){
				mySchemeItem.setIssueOperation(issueOperation); // or: EDIT_ISSUE_OPERATION, VIEW_ISSUE_OPERATION
			}
			mySchemeItem.setFieldScreen(fieldScreen);
			fieldScreenScheme.addFieldScreenSchemeItem(mySchemeItem);			
		}
		 
		
	}*/
	
	public FieldScreenScheme createScreenScheme(final String name,String desc,FieldScreen fieldScreen,
			ScreenableIssueOperation issueOperation){
	
		if(fieldScreen ==null) {
			LOG.error("not able to create screen");
			return null;			
		}
		
		FieldScreenScheme fieldScreenScheme=null;
		
		fieldScreenScheme = Iterables.find(fieldScreenSchemeManager.getFieldScreenSchemes(),
		new Predicate<FieldScreenScheme>() {
            public boolean apply(FieldScreenScheme fieldScreenScheme) {
            	return fieldScreenScheme.getName().equalsIgnoreCase(name);
            }
			},null
		);
		
		if(fieldScreenScheme ==null){
			//create screen scheme
			fieldScreenScheme = new FieldScreenSchemeImpl(fieldScreenSchemeManager);
			fieldScreenScheme.setName(name);
			fieldScreenScheme.setDescription(name);
			//myScheme.store();
			fieldScreenSchemeManager.createFieldScreenScheme(fieldScreenScheme);
		}			
			
		// add screen
		FieldScreenSchemeItem mySchemeItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
		//wanted to make this default
		if(issueOperation!=null){
			mySchemeItem.setIssueOperation(issueOperation); // or: EDIT_ISSUE_OPERATION, VIEW_ISSUE_OPERATION
		}
		mySchemeItem.setFieldScreen(fieldScreen);
		fieldScreenScheme.addFieldScreenSchemeItem(mySchemeItem);
		
		return fieldScreenScheme;
		
				
	}
	
	
	
	public FieldScreen createScreen(final String name,String desc,List<String> fields){		
		
		boolean isExists = false;
		FieldScreen screen = null;
		
		Iterator<FieldScreen> iterator = fieldScreenManager.getFieldScreens().iterator();
		while(iterator.hasNext()){
			FieldScreen fieldScreen = iterator.next();
			if(fieldScreen.getName().equals(name)){
				//fieldScreen.remove();
				isExists = true ;
			}
		}
		
		if(!isExists){
			
			screen = new FieldScreenImpl(fieldScreenManager);
			screen.setName(name);
			screen.setDescription(name);
			screen.store();		
			
			if(screen!=null) {			
				// add field to tab
				FieldScreenTab myTab = screen.addTab("Default");
				///OrderableField Summary = fieldManager.getOrderableField("summary"); // e.g. "assignee", "customfield_12345"
				for(OrderableField field:fieldManager.getOrderableFields()){
					if(fields.contains(field.getName())){
						myTab.addFieldScreenLayoutItem(field.getId());
					}
					//FieldScreenTab myTab = screen.getTab(0);
					//myTab.addFieldScreenLayoutItem(myField.getId());				
				}
				
			}
		}
		
		return screen;
				
	}
	
}
