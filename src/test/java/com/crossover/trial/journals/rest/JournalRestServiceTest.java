package com.crossover.trial.journals.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.crossover.trial.journals.Application;
import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class JournalRestServiceTest {

	private MockMvc mockMvc;
	
	private final static Logger log = Logger.getLogger(JournalRestServiceTest.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private FilterChainProxy springSecurityFilterChain;

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain).build();
		
	}

	protected User getUser(String name) {
		Optional<User> user = userService.getUserByLoginName(name);
		if (!user.isPresent()) {
			fail("user1 doesn't exist");
		}
		return user.get();
	}
	
	public static class MockSecurityContext implements SecurityContext {

        private static final long serialVersionUID = -1386535243513362694L;

        private Authentication authentication;

        public MockSecurityContext(Authentication authentication) {
            this.authentication = authentication;
        }

        @Override
        public Authentication getAuthentication() {
            return this.authentication;
        }

        @Override
        public void setAuthentication(Authentication authentication) {
            this.authentication = authentication;
        }
    }
	/**
	 * This method is used to test the default landing method for the JournalRest layer.
	 * @throws Exception
	 */
	@Test
	public void testDefaultMethod() throws Exception {
		
		UsernamePasswordAuthenticationToken principal = 
                new UsernamePasswordAuthenticationToken("user1", "user1");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
		mockMvc.perform(get("/rest/journals").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$[0].name", is("Medicine")));
		
	}
	/**
	 * The purpose of this method is to simulate a subscriptions list for the current logged in
	 * user. We are asserting on the response to see if the subscription id and name matches
	 * @throws Exception
	 */
	@Test
	public void testUserSubscriptions() throws Exception {
		
		UsernamePasswordAuthenticationToken principal = 
                new UsernamePasswordAuthenticationToken("user1", "user1");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
		mockMvc.perform(get("/rest/journals/subscriptions").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$[2].id", is(3)))
				.andExpect(jsonPath("$[2].name", is("endocrinology")));
		
	}
	/**
	 * The purpose of this method is to simulate a subscription for a given
	 * user in a specific category. We are checking on the response to see if the subscription was successful
	 * @throws Exception
	 */
	@Test
	public void testUserSubscriptionsByCategory() throws Exception {
		
		UsernamePasswordAuthenticationToken principal = 
                new UsernamePasswordAuthenticationToken("user1", "user1");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
		mockMvc.perform(post("/rest/journals/subscribe/1").session(session)).andExpect(status().isOk()).andExpect(status().is(200));
	}
	/**
	 * The system currently responds with an error when a subscriber user tries to read
	 * the publisher user details
	 * @throws Exception
	 */
	@Test
	public void testPublishedList() throws Exception {
		UsernamePasswordAuthenticationToken principal = 
                new UsernamePasswordAuthenticationToken("publisher1","publisher1");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
		mockMvc.perform(get("/rest/journals/published").session(session)).andExpect(status().is(200));
	}
}
