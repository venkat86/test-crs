package com.crossover.trial.journals.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.crossover.trial.journals.model.Journal;
import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.repository.JournalRepository;
import com.crossover.trial.journals.repository.UserRepository;

/**
 * This class is a spring scheduler component that will run exactly at 10pm in the night.
 * It will check for all the journals that have been published since a day before and will 
 * collect the details and compile and send an email to all the users in the system
 * 
 * It is assumed that all user do have an email address
 * @author venka
 *
 */
@Component("scheduleTask")
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
	private UserRepository userRepository;
    
    @Autowired
	private JournalRepository journalRepository;
    
    /**
     * Note the cron rejex to schedule to execute at 10 pm
     */
    @Scheduled(cron = "* * 22 * * *")
    public void sendDailyJournalSummaryEmail() {
        log.info("The scheduled task to send email notificaitons - begins @ ", dateFormat.format(new Date()));

        List<User> usersList =  userRepository.findAll();
        if(usersList == null){
        	return ;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        List<Journal> journalsForToday = (List<Journal>) journalRepository.findByPublishDateAfter(cal.getTime());
        
        if(journalsForToday == null || journalsForToday.size()==0){
        	log.info("The journal size is 0. No new journals added. Email will not be sent");
        	return ;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<b> <h3> Following journals are added to the medical journal system: </h3></b> <br>");
        sb.append("<table border=\"1\">");
        sb.append("<thead> <th align=\"left\"> Journal Name </th> <th align=\"left\"> Journal Category </th> </thead> <tbody> ");
        for(Journal journal: journalsForToday){
        	sb.append("<tr>");
        	sb.append("<td>"+journal.getName()+"</td>" +"<td>"+journal.getCategory().getName()+"</td>");
        	sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        List<User> userList = userRepository.findAll();
        List<String> userEmailingList  = new ArrayList<String>();
        String emailAddress = null;
        for(User user:userList){
        	emailAddress = user.getEmail();
        	if(emailAddress!=null){
        		userEmailingList.add(emailAddress);
        	}
        }
        log.info("Email is being sent to : "+userEmailingList);
        
        EmailNotificationUtil.sendEmail("venkat.odesk86@gmail.com", "Summary of Daily journals  ", (sb.toString()), userEmailingList);
        log.info("The scheduled task to send email notificaitons - ends @ ", dateFormat.format(new Date()));
    }
}