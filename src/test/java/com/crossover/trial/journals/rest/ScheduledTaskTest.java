package com.crossover.trial.journals.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.crossover.trial.journals.Application;
import com.crossover.trial.journals.notification.ScheduledTasks;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ScheduledTaskTest {

	@Autowired
	private ScheduledTasks task ;
	
	@Test
	public void testDailySummaryJob() throws Exception {
		task.sendDailyJournalSummaryEmail();
	}
}
