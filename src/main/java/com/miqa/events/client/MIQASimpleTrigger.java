package com.miqa.events.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Scanner;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

public class MIQASimpleTrigger {
	// private static int timeout = 1000 * 60 * 60 ;
	private static int timeout = 1000 * 60;
	private static final String TIMEOUT_FILE = "timeout.txt";

	public static int getIntVal(char c) {
		if (c == 'A')
			return 1;
		if (c == 'C')
			return 2;
		if (c == 'G')
			return 3;
		if (c == 'T')
			return 4;
		return -1;
	}
	
	
		
	public static void main(String[] args) throws Exception {
		getIntVal('C');
		int timeoutInSeconds = getTimeOut("timeout.txt");
		if (timeoutInSeconds > 0) {
			// timeout = timeoutInSeconds * 60 * 1000;
		}
		JobDetail job = new JobDetail();
		job.setName("dummyJobName");
		job.setJobClass(SendMIQAEvents.class);

		// configure the scheduler time
		SimpleTrigger trigger = new SimpleTrigger();
		trigger.setName("miqaEventPush");
		trigger.setStartTime(new Date(System.currentTimeMillis() + 5000));
		trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
		trigger.setRepeatInterval(timeout);

		// schedule it
		Scheduler scheduler = new StdSchedulerFactory().getScheduler();
		scheduler.start();
		scheduler.scheduleJob(job, trigger);

	}

	private static int getTimeOut(String string) {
		Scanner scanner;
		try {
			scanner = new Scanner(new File(TIMEOUT_FILE));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		int retVal = -1;
		while (scanner.hasNextInt()) {
			retVal = scanner.nextInt();
		}
		return retVal;
	}
}
