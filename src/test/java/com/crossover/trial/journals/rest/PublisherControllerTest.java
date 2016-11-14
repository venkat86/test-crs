package com.crossover.trial.journals.rest;

import static org.junit.Assert.fail;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.crossover.trial.journals.Application;
import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class PublisherControllerTest {

	private MockMvc mockMvc;
	
	private final static Logger log = Logger.getLogger(PublisherControllerTest.class);
	
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
	 * This method is used to test access restrictions. A subscriber cannot upload files.
	 * 
	 * We will get a 403-forbidden error
	 * @throws Exception
	 */

    @Test
    public void testFilePublishFailure() throws Exception {

        MockMultipartFile firstFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());

        UsernamePasswordAuthenticationToken principal = 
                new UsernamePasswordAuthenticationToken("user1", "user1");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
        
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/publisher/publish")
                        .file(firstFile)
                        .param("name", "filename.txt")
                        .param("category", "1").session(session))
                    .andExpect(status().is(403));
                    //.andExpect(content().string("success"));
    }
    /**
	 * This method is used to test the default publish/upload functionality of the journals.
	 * In case of successful upload, the redirection happens to the list page
	 * @throws Exception
	 */

    @Test
    public void testFilePublishSuccess() throws Exception {

        MockMultipartFile firstFile = new MockMultipartFile("file", "filename2.txt", "text/plain", "some xml".getBytes());

        UsernamePasswordAuthenticationToken principal = 
                new UsernamePasswordAuthenticationToken("publisher1", "publisher1");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
        // after successful upload, redirects to the list page
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/publisher/publish")
                        .file(firstFile)
                        .param("name", "filename.txt")
                        .param("category", "1").session(session))
                    .andExpect(status().is(302));
                    //.andExpect(content().string("success"));
    }
    /**
	 * This method is used to test the default publish/upload functionality of the journals.
	 * In case file is uploaded with no content, then the method should fail
	 * @throws Exception
	 */

    @Test
    public void testFilePublishFileNotPresent() throws Exception {

        MockMultipartFile firstFile =  new MockMultipartFile("file", "filename2.txt", "text/plain", "".getBytes());

        UsernamePasswordAuthenticationToken principal = 
                new UsernamePasswordAuthenticationToken("publisher1", "publisher1");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                new MockSecurityContext(principal));
        // after successful upload, redirects to the list page
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/publisher/publish")
                        .file(firstFile)
                        .param("name", "filename.txt")
                        .param("category", "1").session(session))
                    .andExpect(status().is(302));
                    //.andExpect(content().string("success"));
    }
}
