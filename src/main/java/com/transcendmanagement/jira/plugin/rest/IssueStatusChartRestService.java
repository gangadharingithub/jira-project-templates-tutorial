package com.transcendmanagement.jira.plugin.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


@Path("/issueStatusChart")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class IssueStatusChartRestService {
	
	private static final Logger Log = LoggerFactory
			.getLogger(IssueStatusChartRestService.class);
	
	private final IssueManager issueManager;
	private final CustomFieldManager customFieldManager;
	private final SearchProvider searchProvider;
	private final ChangeHistoryManager changeHistoryManager;
	
	private static final String planned_completion  = "Planned Completion";
	private static final String Committed_Completion  = "Committed Completion";
	final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");	
	private final List<String> statuses = Lists.newArrayList("Open","In Progress","Closed");
	
	
	public IssueStatusChartRestService(IssueManager IssueManager,
			CustomFieldManager customFieldManager,SearchProvider searchProvider,
			ChangeHistoryManager changeHistoryManager) throws CreateException{
		this.issueManager=IssueManager;
		this.customFieldManager=customFieldManager;
		this.searchProvider=searchProvider;
		this.changeHistoryManager=changeHistoryManager;		
	}
	
	
	@GET
	@Path ("/data")
	public Response getData(@QueryParam("issueKey") String issueKey) throws SearchException{
		
		//issue Types ==> Problem, Bus Ops,Field Issue,Bug,Deficiency and Milestone ()
		//status ==> new, In Progress,Closed
		//custom fields ==>  "planned completion" and "Committed Completion"
		// create status   Open , Closed
		
		//create a project and associate the above issue types with project
		
		//import the CSV file to create issues in TMAX project, change the date format in that(yyyy-MM-dd).
		//there is a bug in the JIM ,so upgrade the plugin to 7.0.20 
			
		
		MutableIssue issue = issueManager.getIssueObject(issueKey);			
		Date orgStartDate =  new Date(issue.getCreated().getTime());		
		Date endDate = null;
		
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
		//return Response.ok(gson.toJson(getDataPoints(orgStartDate,endDate,issue),IssueStatusChart.class)).build();
		return Response.ok(getDataPoints(orgStartDate,endDate,issue).getJson().toString()).build();		
	}
	
	private IssueStatusChart getDataPoints(Date startDate,Date endDate,Issue issue) throws SearchException{
		
		SearchResults searchResults;
		
		JSONArray dataPoints = new JSONArray();
		SortedSet<Date>  datePoints = Sets.newTreeSet();
		
		JqlClauseBuilder baseQuery = JqlQueryBuilder.newClauseBuilder().defaultAnd().project(issue.getProjectObject().getKey())
				.issueType().in("Problem", "Bus Ops","Field Issue","Bug","Deficiency")
				.and().fixVersion().in(StringUtils.join(issue.getFixVersions().toArray(), ","));
		
		searchResults = searchProvider.search(baseQuery/*.and().status().in("Open","In Progress","Closed")*/.buildQuery(),
				ComponentAccessor.getJiraAuthenticationContext().getUser(), com.atlassian.jira.web.bean.PagerFilter.getUnlimitedFilter());
		
		List<Issue>  issues = searchResults.getIssues();
		
		for(String status : statuses){
			
			for(Issue issue1 : issues){
				
				Date date = getIssueStatusUpateDate(issue1,status);
				
				//TODO we are assuming if no status chnage history item, that this issue is in first of its status.
				if(!isDateAlreadyExists(datePoints,date)){					
					if(date!=null){
						datePoints.add(date);
					}else{
						datePoints.add(issue1.getCreated());
					}
				}
				
				/*if(date!=null && !isDateAlreadyExists(datePoints,date)){
					datePoints.add(date);
				}*/
				
			}
			
		}
		
		Log.info("All Date Points : " + datePoints);		
		
		for (Date date : datePoints){
			
			List<IssueCount>  issuecounts = Lists.newArrayList();
			
			for(String status: statuses){
				issuecounts.add(new IssueCount(status,getIssueCountForStatus(status,date,issues)));
			}			
			dataPoints.put(new DataPoint(date,issuecounts).getJson());			
		}		
		return new IssueStatusChart(startDate,endDate,dataPoints);
		
	}
	
	private int getIssueCountForStatus(final String status,final Date date,List<Issue> issues) {		
		
		return Iterables.size(Iterables.filter(issues, new Predicate<Issue>() {
		    @Override
		    public boolean apply(Issue issue) {
		    	
		    	//
		    	Date updateDate = getIssueStatusUpateDate(issue,status);		    	
	    		return updateDate!=null && DateTimeComparator.getDateOnlyInstance().compare(updateDate, date) <= 0 ||
	    				//this covers issues in open(first state) state.
	    				updateDate==null && DateTimeComparator.getDateOnlyInstance().compare(issue.getCreated(), date) <= 0
	    				&& issue.getStatusObject().getName().equalsIgnoreCase(status);
	    		
		    	/*if(status.equalsIgnoreCase("Open")){
		    		return DateTimeComparator.getDateOnlyInstance().compare(issue.getCreated(), date) <= 0 ;
		    						    		
		    	}else{		    		
		    		Date updateDate = getIssueStatusUpateDate(issue,status);
		    		return updateDate!=null && DateTimeComparator.getDateOnlyInstance().compare(updateDate, date) <= 0 ;
		    				
		    	}*/
		    }
		}));
		
	}


	private boolean isDateAlreadyExists(Set<Date> dates,final Date inputDate){
		
		return Iterables.any(dates, new Predicate<Date> () {
	        public boolean apply(Date date) {				
				return DateTimeComparator.getDateOnlyInstance().compare(inputDate, date)==0;
	        }
	    });
		
	}
	
	private Date getIssueStatusUpateDate(Issue issue,String status){
		
		Date date = null;
		
		List<ChangeItemBean> statusChHistory = changeHistoryManager.getChangeItemsForField(issue, "status");
		
		for( ChangeItemBean changeItemBean : statusChHistory){
			
			Date updatedDate = new Date(changeItemBean.getCreated().getTime());
			
			if(changeItemBean.getToString().equals(status)){
				date = updatedDate;
				break;
			}
			
		}		
		
		return date;
	}
	
	@XmlRootElement
	public class IssueStatusChart{
		
		@XmlElement		
		String startDate;
		
		@XmlElement	
		String endDate;
		
		@XmlElement
		JSONArray dataPoints;
		
		public IssueStatusChart(Date startDate, Date endDate,
				JSONArray dataPoints) {
			super();
			this.startDate = formatter.format(startDate).toString();
			this.endDate = formatter.format(endDate).toString();
			this.dataPoints = dataPoints;
		}
		
		JSONObject getJson(){	
			
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("startDate", startDate);
				jsonObj.put("endDate", endDate);
				jsonObj.put("dataPoints", dataPoints);				
			} catch (JSONException e) {
				Log.error("error while creating data", e);
				return null;
			}			
			return jsonObj;
		}
	}
	
	@XmlRootElement
	public class DataPoint{
		
		@XmlElement
		String date;
		
		@XmlElement
		List<IssueCount> statusCounts = Lists.newArrayList();
		
		public DataPoint(Date date,List<IssueCount> issuecounts){
			super();			
			this.date=formatter.format(date).toString();
			this.statusCounts = issuecounts;
		}
		
		public JSONObject getJson(){
			
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("date", this.date);
				for(IssueCount issueCount: this.statusCounts){
					//if(issueCount.status.equalsIgnoreCase("closed")||issueCount.status.equalsIgnoreCase("In Progress"))
					jsonObj.put(issueCount.status, issueCount.count);
				}
			} catch (JSONException e) {
				Log.error("error while creating json String :", e );
				return null;
			}
			
			return jsonObj;
		}
	}
	
	class IssueCount {
		
		public IssueCount(String status2, int issueCountForStatus) {
			this.status=status2;
			this.count=issueCountForStatus;
		}

		@XmlElement
		String status;
		
		@XmlElement
		int count;		
	}

}
