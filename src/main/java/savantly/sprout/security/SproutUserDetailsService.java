package savantly.sprout.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import savantly.sprout.domain.EmailAddress;
import savantly.sprout.domain.SproutUser;
import savantly.sprout.repositories.emailAddress.EmailAddressRepository;
import savantly.sprout.repositories.user.UserRepository;

@Component
public class SproutUserDetailsService implements UserDetailsService, InitializingBean {

	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	UserRepository userRepository;
	@Autowired
	EmailAddressRepository emailAddressRepository;
	
	private String password = "password";
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		SproutUser user = userRepository.findOne(username);
		if(user == null) {
			throw new UsernameNotFoundException(String.format("User not found: %s", username));
		}
		else{
			return user;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
        try{
        	loadUserByUsername("anonymousUser");
        } catch (UsernameNotFoundException ex){
        	List<Role> authorities = new ArrayList<Role>(1);
            authorities.add(new Role(Roles.ROLE_ANONYMOUS));
            SproutUser userDetails = new SproutUser("anonymousUser", "", "Anonymous", "User", authorities);
            
            EmailAddress emailAddress = new EmailAddress("jdbranham1@hotmail.com");
            emailAddressRepository.insert(emailAddress);
            
            userDetails.addEmailAddress(emailAddress);
            userRepository.save(userDetails);
        }
        
        try{
        	loadUserByUsername("user");
        } catch (UsernameNotFoundException ex){
        	List<Role> authorities = new ArrayList<Role>(1);
            authorities.add(new Role(Roles.ROLE_USER));
            SproutUser userDetails = new SproutUser("user", passwordEncoder.encode(password), "Test",  "User", authorities);
            
            EmailAddress emailAddress = new EmailAddress("jdbranham2@hotmail.com");
            emailAddressRepository.insert(emailAddress);
            
            userDetails.addEmailAddress(emailAddress);
            userRepository.save(userDetails);
        }
        
        try{
        	loadUserByUsername("admin");
        } catch (UsernameNotFoundException ex){
        	List<Role> authorities = new ArrayList<Role>(1);
            authorities.add(new Role(Roles.ROLE_ADMIN));
            SproutUser userDetails = new SproutUser("admin", passwordEncoder.encode(password), "Admin",  "User", authorities);
            
            EmailAddress emailAddress = new EmailAddress("jdbranham@hotmail.com");
            emailAddressRepository.insert(emailAddress);
            
            userDetails.addEmailAddress(emailAddress);
            userRepository.save(userDetails);
        }
	}
}