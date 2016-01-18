package com.transcendmanagement.jira.plugin.rest;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.LocalDate;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Path("/productPhasesChart")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class ProductPhasesChartRestService {
	
	private static final Logger LOG = Logger.getLogger(ProductPhasesChartRestService.class);
	
	private final IssueManager issueManager;
	private final CustomFieldManager customFieldManager;
	private final SearchProvider searchProvider;
	private final ChangeHistoryManager changeHistoryManager;
	final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final String planned_completion  = "Planned Completion";
	private static final String Committed_Completion  = "Committed Completion";
	
	
	private static final String projected_start = "Projected Start";
	private static final String projected_finish  = "Projected Finish";
	
	private static final String taskGroup  = "Task Group";
	
	private static final String issueTypeCategory  = "Task";
	
	private static CustomField cf_taskGroup;
	private static CustomField cf_projected_start_date;
	private static CustomField cf_projected_finish_date;
	
	/*	
	 * Data Setup :
	 * 
	 * 
	issue type categories 
	-->Change,Task,Inventory,Data,Issue,Requirement
	
	issue type category : Task
	-->Analysis,Interview,Research,Sub-task,Sub-test,Improvement,Milestone,New Feature,Task,Test,Work Order	
	
	*/	
	
	public ProductPhasesChartRestService(IssueManager IssueManager,
			CustomFieldManager customFieldManager,SearchProvider searchProvider,
			ChangeHistoryManager changeHistoryManager){
		this.issueManager=IssueManager;
		this.customFieldManager=customFieldManager;
		this.searchProvider=searchProvider;
		this.changeHistoryManager=changeHistoryManager;
		
		cf_taskGroup = customFieldManager.getCustomFieldObjectByName(taskGroup);
		cf_projected_finish_date = customFieldManager.getCustomFieldObjectByName(projected_finish);
	}
	
	
	@GET
	@Path ("/data")
	public Response getData(@QueryParam("issueKey") String issueKey) throws SearchException{
		
		// requirements issue types 
		MutableIssue issue = issueManager.getIssueObject(issueKey);		
		Date startDate = new Date(issue.getCreated().getTime());		
		Date endDate = null;
		List<DataPoint> dataPoints = Lists.newArrayList();
		//TODO remove already exists in constructor.
		cf_taskGroup = customFieldManager.getCustomFieldObjectByName(taskGroup);
		
	    Object planned_completion_value = customFieldManager.getCustomFieldObjectByName(planned_completion).getValue(issue);	    
	    Object Committed_Completion_value = customFieldManager.getCustomFieldObjectByName(Committed_Completion).getValue(issue);
	    
	    
		if(planned_completion_value instanceof Date 
				&& Committed_Completion_value instanceof Date){
			Date date1 = (Date) planned_completion_value;
			Date date2 = (Date) Committed_Completion_value;
			if(date1.compareTo(date2) > 0){
				endDate = date1;
			}else{
				endDate = date2;
			}
		}
		
		//dataPoints = getDataPoint(issue);
		
		//test		
		dataPoints.add(new DataPoint("Scoping", "Open", new DateTime("2015-06-01").toDate(), new DateTime("2015-06-30").toDate()));
		dataPoints.add(new DataPoint("Scoping", "Closed", new DateTime("2015-08-01").toDate(), new DateTime("2015-08-30").toDate()));
		dataPoints.add(new DataPoint("Scoping", "In Progress", new DateTime("2015-07-01").toDate(), new DateTime("2015-07-30").toDate()));
		
		Collections.sort(dataPoints,new Comparator<DataPoint>() {			
			public int compare(DataPoint p1, DataPoint p2) {
		        return p1.endDate.compareTo(p2.endDate);
		    }
			
		});		
		
		return Response.ok(new ProductPhasesChart(startDate,endDate,dataPoints)).build();		
	}
	
	private List<DataPoint> getDataPoint(Issue issue) throws SearchException{
		
		SearchResults searchResults;		
		List<Issue> issues;
		List<DataPoint> dataPoints = Lists.newArrayList();
		
		JqlClauseBuilder baseQuery = JqlQueryBuilder.newClauseBuilder().defaultAnd().project(issue.getProjectObject().getKey())
				.issueType().in("Analysis","Interview","Research","Sub-task","Sub-test","Improvement","Milestone","New Feature","Task","Test","Work Order")
				.and().fixVersion().in(StringUtils.join(issue.getFixVersions().toArray(), ","));				
		
		searchResults = searchProvider.search(baseQuery.buildQuery(),
					ComponentAccessor.getJiraAuthenticationContext().getUser(), com.atlassian.jira.web.bean.PagerFilter.getUnlimitedFilter());			
		issues = searchResults.getIssues();
		
		/*Task Group: 		
			Scoping 
			Architecture
			Conceptual Design
			Detailed Design
			Verification
			Validation
			Delivery*/
		
		dataPoints.addAll(createDataPoint("Scoping", issues));
		/*dataPoints.addAll(createDataPoint("Architecture", issues));
		dataPoints.addAll(createDataPoint("Conceptual Design", issues));
		dataPoints.addAll(createDataPoint("Detailed Design", issues));
		dataPoints.addAll(createDataPoint("Verification", issues));
		dataPoints.addAll(createDataPoint("Validation", issues));
		dataPoints.addAll(createDataPoint("Delivery", issues));*/
		
		return dataPoints;		
				
	}
	
	private List<DataPoint> createDataPoint(String taskGroupValue,List<Issue> issues){
		
		Date date1 = null,date2 = null;
		List<DataPoint> taskGroupDataPoints = Lists.newArrayList();
		
		date1 = getMinOrMaxDate(getIssuesByStatus(getIssuesByTaskGroup(issues,taskGroupValue),"Closed"),0);
		date2 = getMinOrMaxIssueTransitionDate(getIssuesByStatus(getIssuesByTaskGroup(issues,taskGroupValue),"Closed"),"Closed",1);
		
		if(date1!=null || date2!=null){
			taskGroupDataPoints.add(new DataPoint(taskGroupValue, "Closed", date1, date2));
		}
		
		date1  = getMinOrMaxDate(getIssuesByStatus(getIssuesByTaskGroup(issues,taskGroupValue),"In Progress"),0);
		date2 = getMinOrMaxIssueCfDate(getIssuesByStatus(getIssuesByTaskGroup(issues,taskGroupValue),"In Progress"),1,cf_projected_finish_date);
		
		if(date1!=null || date2!=null){
			taskGroupDataPoints.add(new DataPoint(taskGroupValue, "In Progress", date1, date2));
		}	
			
		date1 = getMinOrMaxIssueCfDate(
				getIssuesByStatus(
						getIssuesByTaskGroup(issues,taskGroupValue),"Open"),0,cf_projected_start_date);
		date2 = getMinOrMaxIssueCfDate(getIssuesByStatus(getIssuesByTaskGroup(issues,taskGroupValue),"Open"),1,cf_projected_finish_date);
		
		if(date1!=null || date2!=null){
			taskGroupDataPoints.add(new DataPoint(taskGroupValue, "Open", date1, date2));
		}
		
		return taskGroupDataPoints;
		
	}
	
	private List<Issue> getIssuesByTaskGroup(List<Issue> issues,final String taskGroup){
		
		List<Issue> filteredIssues =   Lists.newArrayList(Iterables.filter(issues, new Predicate<Issue>() {
		    @Override
		    public boolean apply(Issue issue) {
		    	
		    	Object customfield_value  = customFieldManager.getCustomFieldObjectByName(cf_taskGroup.getName()).getValue(issue);
		    	
		    	if(customfield_value != null 
		    			&& customfield_value instanceof Option){
		    		
		    		Option opt = (Option) customfield_value;		    		
		    		return taskGroup.equalsIgnoreCase(opt.getValue());
		    	}
		      
		    	return false;
		    	
		    }
		}));
		
		if(filteredIssues==null){
			filteredIssues = Collections.emptyList();
		}
		
		return filteredIssues;
	}
	
	private Date getMinOrMaxDate(List<Issue> issues,int minMaxFlag){
		
		if(issues.isEmpty()){
			return null;
		}
		
		List<Date> dates = Lists.newArrayList(Lists.transform(issues,new Function<Issue, Date>() {
			@Override
			public Date apply(final Issue issue) {
				return issue.getCreated();
						
			}
		}));
		
		Collections.sort(dates);		
		return minMaxFlag==0?dates.get(0):dates.get(dates.size()-1);
		
	}
	
	private Date getMinOrMaxIssueCfDate(List<Issue> issues,int minMaxFlag,final CustomField cf){
		

		if(issues.isEmpty()){
			return null;
		}
		
		List<Date> dates = Lists.newArrayList(Lists.transform(issues,new Function<Issue, Date>() {
			@Override
			public Date apply(final Issue issue) {								
				return issue.getCreated();
						
			}
		}));
		
		Collections.sort(Lists.newArrayList(dates));		
		return minMaxFlag==0?dates.get(0):dates.get(dates.size()-1);
		
	}
	
	private Date getMinOrMaxIssueTransitionDate(List<Issue> issues,final String status,int minMaxFlag){
		

		if(issues.isEmpty()){
			return null;
		}
		
		// get list of transition(done) dates for issue.
		List<Date> dates = Lists.newArrayList(Lists.transform(issues,new Function<Issue, Date>() {
			@Override
			public Date apply(final Issue issue) {
				//return issue.getCreated();
				return getIssueStatusUpateDate(issue,status);
						
			}
		}));
		
		Collections.sort(Lists.newArrayList(dates));
		return minMaxFlag==0?dates.get(0):dates.get(dates.size()-1);
		
	}
	
	private Date getIssueStatusUpateDate(Issue issue,String status){
		
		Date date = null;
		
		List<ChangeItemBean> statusChHistory = changeHistoryManager.getChangeItemsForField(issue, "status");
		
		for( ChangeItemBean changeItemBean : statusChHistory){
			
			Date updatedDate = new Date(issue.getCreated().getTime());
			
			if(changeItemBean.getToString().equals(status)){
				date = updatedDate;
				break;
			}
			
		}		
		
		return date;
	}
	
	private List<Issue> getIssuesByStatus(List<Issue> issues,final String status){
		
		List<Issue> filteredIssues =  Lists.newArrayList(Iterables.filter(issues, new Predicate<Issue>() {
		    @Override
		    public boolean apply(Issue issue) {
		    	
		    	LOG.info("Staus : Issue Status :" + status + ":"  + issue.getStatusObject().getName());
		    	
		    	return issue.getStatusObject().getName().equalsIgnoreCase(status);
		    	
		    }
		}));
		
		if(filteredIssues == null){
			filteredIssues = Collections.emptyList();
		}
			
		return filteredIssues;
	}
	
	
	@XmlRootElement
	public class ProductPhasesChart{

		@XmlElement
		String startDate;
		
		@XmlElement
		String endDate;
		
		@XmlElement
		List<DataPoint> dataPoints;
		
		public ProductPhasesChart(Date startDate, Date endDate,
				List<DataPoint> dataPoints) {
			super();			
			this.startDate=formatter.format(startDate).toString();
			this.endDate=formatter.format(endDate).toString();			
			this.dataPoints = dataPoints;
		}
		
	}
	
	@XmlRootElement
	public class DataPoint{	
		
		//TODO use domain specific name rather than gantt lib name.
		@XmlElement
		String taskName;

		@XmlElement
		String status;		
		
		@XmlElement
		String startDate;
		
		@XmlElement
		String endDate;
		
		public DataPoint(String taskName,String status, Date date1, Date date2) {
			super();
			this.taskName=taskName;
			this.status = status;
			this.startDate = formatter.format(date1).toString();
			this.endDate = formatter.format(date2).toString();
		}		
		
	}

}
