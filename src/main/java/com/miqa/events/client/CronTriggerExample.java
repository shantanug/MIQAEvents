package com.miqa.events.client;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

public class CronTriggerExample 
{
    public static void main( String[] args ) throws Exception
    {
    	JobDetail job = null;
    	CronTrigger trigger = null;
    	Scheduler scheduler = null;
    	
    	/*job = new JobDetail();
    	job.setName("dummyJobName");
    	job.setJobClass(SendMIQAEvents.class);
    	    	
    	trigger = new CronTrigger();
    	trigger.setName("dummyTriggerName");
    	trigger.setCronExpression("0/30 * * * * ?");
    	
    	//schedule it
    	scheduler = new StdSchedulerFactory().getScheduler();
    	scheduler.start();
    	scheduler.scheduleJob(job, trigger);
    
    	*/
    	job = new JobDetail();
    	job.setName("attemptSummary");
    	if(isScholaranium())
    		job.setJobClass(ScholAttemptsSummaryGenerator.class);
	    else
    		job.setJobClass(AttemptsSummaryGenerator.class);
    	    	
    	trigger = new CronTrigger();
    	trigger.setName("attemptSummary");
    	trigger.setCronExpression("0 0/1 * * * ?");
    	
    	//schedule it
    	scheduler = new StdSchedulerFactory().getScheduler();
    	scheduler.start();
    	scheduler.scheduleJob(job, trigger);
    }

	private static boolean isScholaranium() {
		// TODO Auto-generated method stub
		return true;
	}
}
