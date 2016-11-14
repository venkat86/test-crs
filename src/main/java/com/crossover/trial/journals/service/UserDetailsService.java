package com.crossover.trial.journals.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.repository.UserRepository;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(UserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.info("Authenticating inside custom method {}", login);
        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
       
        User loggedInUser = userRepository.findByLoginName(lowercaseLogin);
     
        CurrentUser authenticatedUser = new CurrentUser(loggedInUser);
        log.info("REturing the user ==>"+loggedInUser);
        return authenticatedUser;
    }
}
