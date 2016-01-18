package com.transcendmanagement.jira.plugin.rest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Path("/requirementStatusChart")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class RequirementStatusChartRestService {
	
	private static final Logger Log = LoggerFactory
			.getLogger(RequirementStatusChartRestService.class);
	
	private final IssueManager issueManager;
	private final CustomFieldManager customFieldManager;
	private final SearchProvider searchProvider;
	
	private static final String planned_completion  = "Planned Completion";
	private static final String Committed_Completion  = "Committed Completion";
	
	private static final String Validation_planned_date  = "Validation planned date";
	private static final String Implementation_planned_date  = "Implementation planned date";
	private static final String Verification_Planned_Date  = "Verification Planned Date";
	
	private static final String Validation_actual_date  = "Validation Actual Date";
	private static final String Implementation_actual_date  = "Implementation Actual Date";
	private static final String Verification_actual_Date  = "Verification Actual Date";
	final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	
	
	public RequirementStatusChartRestService(IssueManager IssueManager,
			CustomFieldManager customFieldManager,SearchProvider searchProvider){
		this.issueManager=IssueManager;
		this.customFieldManager=customFieldManager;
		this.searchProvider=searchProvider;
	}
	
	
	@GET
	@Path ("/data")
	public Response getData(@QueryParam("issueKey") String issueKey) throws SearchException{
		
		// requirements issue types 
		MutableIssue issue = issueManager.getIssueObject(issueKey);		
		Date startDate =  new Date(issue.getCreated().getTime());		
		Date endDate = null;
		///List<DataPoint> dataPoints = Lists.newArrayList();
		SearchResults searchResults;
		List<Issue> issues;
		JSONObject obj = null;
		
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
			
			JqlClauseBuilder baseQuery = JqlQueryBuilder.newClauseBuilder().defaultAnd().project(issue.getProjectObject().getKey())
					.issueType().in("Requirement")
					.and().fixVersion().in(StringUtils.join(issue.getFixVersions().toArray(), ","));
			
			searchResults = searchProvider.search(baseQuery/*.and().customField(cf_Validation_planned_date.getIdAsLong()).eq(date.toDate())*/.buildQuery(),
					ComponentAccessor.getJiraAuthenticationContext().getUser(), com.atlassian.jira.web.bean.PagerFilter.getUnlimitedFilter());			
		
			issues = searchResults.getIssues();	
			obj = new JSONObject();
			
			try {
				obj.put("planVal", this.getMap(Validation_planned_date, issues));
				obj.put("actVal", this.getMap(Validation_actual_date, issues));
				obj.put("planVer", this.getMap(Verification_Planned_Date, issues));
				obj.put("actVer", this.getMap(Verification_actual_Date, issues));
				obj.put("planImpl", this.getMap(Implementation_planned_date, issues));
				obj.put("actImpl", this.getMap(Implementation_actual_date, issues));
			} catch (JSONException e) {
				Log.error("error while creating json String :", e );				
			}
		}
		
		return Response.ok(new RequirementStatusChart(startDate, endDate, obj).getJson().toString()).build();		
	}
	
	
	
	
	private JSONArray getMap(String cfName,List<Issue> issues){	
		
		JSONArray jsonArray = new JSONArray();				
		List<Date> list = new ArrayList<Date>(this.getDates(issues,cfName));
		Collections.sort(list);
		try {			
			for (Date temp : list) {
				JSONObject jsonObj = new JSONObject();
				String date=formatter.format(temp).toString();
				jsonObj.put("date",date);
				jsonObj.put("count",Collections.frequency(list, temp));
				jsonArray.put(jsonObj);
			}			
		} catch (JSONException e) {
			Log.error("error while creating json String :", e );
			return null;
		}
		
		return jsonArray;
		
	}
	
	
	private List<Date> getDates(List<Issue> issues,final String CfName){
		
		List<Date> dates =  Lists.transform(issues,new Function<Issue, Date>() {
			@Override
			public Date apply(final Issue issue) {				
				Object customfield_value  = customFieldManager.getCustomFieldObjectByName(CfName).getValue(issue);
				return (Date) customfield_value;
			}
		});
		
		return dates;
		
	}
		
	public class RequirementStatusChart{
		
		String startDate;		
		String endDate;		
		JSONObject dataPoints;
		
		public RequirementStatusChart(Date startDate, Date endDate,
				JSONObject dataPoints) {
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
				return jsonObj;
			}			
			return jsonObj;
		}
		
	}
	
}
