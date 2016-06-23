package savantly.sprout.web.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.filter.CompositeFilter;

import savantly.sprout.repositories.user.UserRepository;
import savantly.sprout.security.oauth.ClientResources;
import savantly.sprout.security.oauth.GithubPrincipalExtractor;

@Configuration
@EnableOAuth2Client
public class OAuthConfiguration {

	@Autowired
	UserDetailsService userDetailsService;
	@Autowired
	OAuth2ClientContext oauth2ClientContext;
	@Autowired 
	UserRepository userRepository;

	@Bean(name="githubClient")
	@ConfigurationProperties("github")
	ClientResources github() {
		ClientResources resources = new ClientResources(oauth2ClientContext);
		resources.setPrincipalExtractor(new GithubPrincipalExtractor(userRepository, resources.getRestTemplate()));
		return resources;
	}
	
	
/*	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(Filter ssoFilter) {
	  FilterRegistrationBean registration = new FilterRegistrationBean();
	  registration.setFilter(ssoFilter);
	  registration.setOrder(-100);
	  return registration;
	}*/

	@Bean(name="ssoFilter")
	public Filter ssoFilter() {
		CompositeFilter filter = new CompositeFilter();
		List<Filter> filters = new ArrayList<>();
		/* filters.add(ssoFilter(facebook(), "/login/facebook")); */
		filters.add(ssoFilter(github(), "/login/github"));
		filter.setFilters(filters);
		return filter;
	}

	private Filter ssoFilter(ClientResources client, String path) {
		UserInfoTokenServices userInfoTokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(), client.getClient().getClientId());
		userInfoTokenServices.setPrincipalExtractor(client.getPrincipalExtractor());
		OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
		filter.setRestTemplate(client.getRestTemplate());
		filter.setTokenServices(userInfoTokenServices);
		return filter;
	}
}
