package com.crossover.trial.journals.notification;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.crossover.trial.journals.model.Journal;
import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.repository.JournalRepository;
import com.crossover.trial.journals.repository.UserRepository;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
	private UserRepository userRepository;
    
    @Autowired
	private JournalRepository journalRepository;
    
    @Scheduled(cron = "0/30 * * * * *")
    public void reportCurrentTime() {
        log.info("The scheduled task to send email notificaitons - begins @ ", dateFormat.format(new Date()));

        List<User> usersList =  userRepository.findAll();
        if(usersList == null){
        	return;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        List<Journal> journalsForToday = (List<Journal>) journalRepository.findByPublishDateAfter(cal.getTime());
        
        if(journalsForToday == null || journalsForToday.size()==0){
        	log.info("The journal size is 0. No new journals added. Email will not be sent");
        	return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<b> <h3> Following journals are added to the medical journal system: </h3></b> <br>");
        for(Journal journal: journalsForToday){
        	sb.append(journal.getName() + "<br>");
        }
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