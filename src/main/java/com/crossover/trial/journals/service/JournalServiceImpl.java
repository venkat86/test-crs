package com.crossover.trial.journals.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.crossover.trial.journals.controller.PublisherController;
import com.crossover.trial.journals.model.Category;
import com.crossover.trial.journals.model.Journal;
import com.crossover.trial.journals.model.Publisher;
import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.notification.EmailNotificationUtil;
import com.crossover.trial.journals.repository.CategoryRepository;
import com.crossover.trial.journals.repository.UserRepository;
import com.crossover.trial.journals.model.Subscription;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.crossover.trial.journals.repository.JournalRepository;
import com.crossover.trial.journals.repository.SubscriptionRepository;

@Service
public class JournalServiceImpl implements JournalService {

	private final static Logger log = Logger.getLogger(JournalServiceImpl.class);

	@Autowired
	private JournalRepository journalRepository;
	
	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public List<Journal> listAll(User user) {
		User persistentUser = userRepository.findOne(user.getId());
		List<Subscription> subscriptions = persistentUser.getSubscriptions();
		if (subscriptions != null) {
			List<Long> ids = new ArrayList<>(subscriptions.size());
			subscriptions.stream().forEach(s -> ids.add(s.getCategory().getId()));
			return journalRepository.findByCategoryIdIn(ids);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public List<Journal> publisherList(Publisher publisher) {
		Iterable<Journal> journals = journalRepository.findByPublisher(publisher);
		log.info("Journals from DB is :"+journals);
		return StreamSupport.stream(journals.spliterator(), false).collect(Collectors.toList());
	}

	@Override
	public Journal publish(Publisher publisher, Journal journal, Long categoryId) throws ServiceException {
		Category category = categoryRepository.findOne(categoryId);
		if(category == null) {
			throw new ServiceException("Category not found");
		}
		journal.setPublisher(publisher);
		journal.setCategory(category);
		try {
			Journal journalNew = journalRepository.save(journal);
			List<Subscription> subscriptions  = (List<Subscription>)subscriptionRepository.findByCategory(category);
			if(subscriptions == null || subscriptions.size() ==0){
				log.info("No subscribers present. Hence, email notification will not be sent");
				return journalNew;
			}
			List<String> emailList = new ArrayList<String>();
			String email = null;
			for(Subscription sub: subscriptions){
				email = sub.getUser().getEmail();
				log.info("Email added to the list is ==>"+email);
				if(email != null ){
					emailList.add(email);	
				}
			}
			log.debug("Before sending email - journal added");
			EmailNotificationUtil util = new EmailNotificationUtil();
			util.sendEmail("venkat.odesk86@gmail.com","New journal - "+journal.getName()+" - added", "A new journal has been added. Please check your inbox",emailList);
			log.debug("After sending email - journal added");
			return journalNew;
		} catch (DataIntegrityViolationException e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}

	@Override
	public void unPublish(Publisher publisher, Long id) throws ServiceException {
		Journal journal = journalRepository.findOne(id);
		if (journal == null) {
			throw new ServiceException("Journal doesn't exist");
		}
		String filePath = PublisherController.getFileName(publisher.getId(), journal.getUuid());
		File file = new File(filePath);
		if (file.exists()) {
			boolean deleted = file.delete();
			if (!deleted) {
				log.error("File " + filePath + " cannot be deleted");
			}
		}
		if (!journal.getPublisher().getId().equals(publisher.getId())) {
			throw new ServiceException("Journal cannot be removed");
		}
		journalRepository.delete(journal);
	}
}
