package com.crossover.trial.journals.notification;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

/**
 * The purpose of this method is to send email notifications to a given email address(es)
 * 
 * @author venka
 *
 */
public class EmailNotificationUtil {

	private final static Logger log = Logger.getLogger(EmailNotificationUtil.class);
	
	/**
	 * Thr purpose of this method is to send an email to a list of recepients with a given
	 * subject and content
	 * @param fromAddress
	 * @param subject
	 * @param content
	 * @param toAddresses
	 */
	public static void sendEmail(String fromAddress,String subject, String content, List<String> toAddresses) {

		log.debug("Entering the generic send email method");
		// TODO Auto-generated method stub

		final String username = "venkat.odesk86@gmail.com";
		final String password = "pvtltd456";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromAddress));
			message.addHeader("Disposition-Notification-To","venkat.odesk86@gmail.com");
			InternetAddress[] toAddress = new InternetAddress[toAddresses.size()];
		    for (int i = 0; i < toAddresses.size(); i++) {
		    	toAddress[i] = new InternetAddress(toAddresses.get(i));
		    }
			message.setRecipients(Message.RecipientType.TO,toAddress
				);
			message.setSubject(subject);
			message.setContent(content, "text/html; charset=utf-8");

			Transport.send(message);

			log.info("Email sent successfully");

		} catch (MessagingException e) {
			// this error will be ignored for now
			log.error("Error occured in sending email - "+e.getMessage());
		}
	
	
	
	}
	
}
