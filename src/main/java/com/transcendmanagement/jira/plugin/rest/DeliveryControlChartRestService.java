package com.transcendmanagement.jira.plugin.rest;

import java.text.SimpleDateFormat;
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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.google.common.collect.Lists;

@Path("/deliveryControlChart")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class DeliveryControlChartRestService {
	
	private final IssueManager issueManager;
	private final CustomFieldManager customFieldManager;
	
	private final ChangeHistoryManager changeHistoryManager;
	final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final String planned_completion  = "Planned Completion";
	private static final String Committed_Completion  = "Committed Completion";	
	
	private static final String completion_Probability  = "Completion Probability";	
	
	public DeliveryControlChartRestService(IssueManager IssueManager,
			CustomFieldManager customFieldManager,
			ChangeHistoryManager changeHistoryManager){
		this.issueManager=IssueManager;
		this.customFieldManager=customFieldManager;		
		this.changeHistoryManager=changeHistoryManager;
	}
	
	@GET
	@Path ("/data")
	public Response getData(@QueryParam("issueKey") String issueKey) throws SearchException{
		
		//Problem, Bus Ops,Field Issue,Bug,Deficiency
		//new In Progress,Closed
		MutableIssue issue = issueManager.getIssueObject(issueKey);		
		LocalDate startDate =  new LocalDate(issue.getCreated());
		LocalDate endDate = null;
		List<DataPoint> dataPoints = Lists.newArrayList();
		
	    Object planned_completion_value = customFieldManager.getCustomFieldObjectByName(planned_completion).getValue(issue);	    
	    Object Committed_Completion_value = customFieldManager.getCustomFieldObjectByName(Committed_Completion).getValue(issue);
	    
	    
		if(planned_completion_value instanceof Date 
				&& Committed_Completion_value instanceof Date){
			Date date1 = (Date) planned_completion_value;
			Date date2 = (Date) Committed_Completion_value;
			if(date1.compareTo(date2) > 0){
				endDate = new LocalDate(date1);
			}else{
				endDate = new LocalDate(date2);
			}
			
		}
			
		List<ChangeItemBean> probabilityChHistory = changeHistoryManager.getChangeItemsForField(issue, completion_Probability);
		
		for( ChangeItemBean probabilityChItemBean : probabilityChHistory){
			
			dataPoints.add(new DataPoint(probabilityChItemBean.getCreated(),
					Integer.parseInt(probabilityChItemBean.getTo())));
			
		}
		
		return Response.ok(new DeliveryControlChart(startDate,endDate,dataPoints) ).build();		
	}
	
	
	
	@XmlRootElement
	public class DeliveryControlChart{

		@XmlElement
		LocalDate startDate;
		
		@XmlElement
		LocalDate endDate;
		
		@XmlElement
		List<DataPoint> dataPoints;
		
		public DeliveryControlChart(LocalDate startDate, LocalDate endDate,
				List<DataPoint> dataPoints) {
			super();
			this.startDate = startDate;
			this.endDate = endDate;
			this.dataPoints = dataPoints;
		}
		
	}
	
	@XmlRootElement
	public class DataPoint{		

		@XmlElement
		String date;
		
		@XmlElement
		int probability;			
		
		public DataPoint(Date date, int probability) {
			super();
			//this.date = date;
			this.date=formatter.format(date).toString();
			this.probability = probability;
		}
	}

}
