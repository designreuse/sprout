package net.savantly.sprout.domain.user.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.savantly.sprout.domain.emailAddress.EmailAddress;
import net.savantly.sprout.domain.emailAddress.repository.EmailAddressRepository;
import net.savantly.sprout.domain.user.OAuthAccount;
import net.savantly.sprout.domain.user.SproutUser;
import net.savantly.sprout.security.Role;

public class UserRepositoryImpl implements UserRepositoryCustom {
	
	@Autowired
    private MongoTemplate mongoTemplate;
	@Autowired(required=true)
	PasswordEncoder encoder;
	@Autowired
	EmailAddressRepository emailAddressRepository;

	/**
	 * Set the password in cleartext
	 */
	@Override
	public SproutUser insert(SproutUser sproutUser) {
		if(sproutUser.getAuthorities().isEmpty()){
			Set<Role> authorities = new HashSet<Role>(1);
			authorities.add(new Role("ROLE_USER"));
	        sproutUser.setAuthorities(authorities);
		}
        sproutUser.setPassword(encoder.encode(sproutUser.getPassword()));
        sproutUser.setAccountNonExpired(true);
        sproutUser.setAccountNonLocked(true);
        sproutUser.setCredentialsNonExpired(true);
        sproutUser.setEnabled(true);
		mongoTemplate.insert(sproutUser);
		return sproutUser;
	}
	
	
	/**
	 * Insert a new user with a generated username and password
	 */
	@Override
	public SproutUser insert(String firstName, String lastName, EmailAddress emailAddress){
		SproutUser sproutUser = new SproutUser(UUID.randomUUID().toString(), UUID.randomUUID().toString(), firstName, lastName);
		sproutUser.addEmailAddress(emailAddress);
		return insert(sproutUser);
	}
	
	@Override
	public SproutUser insert(String firstName, String lastName, OAuthAccount oauthAccount, Collection<EmailAddress> emailAddresses ){
		SproutUser sproutUser = new SproutUser(UUID.randomUUID().toString(), UUID.randomUUID().toString(), firstName, lastName);
		sproutUser.addEmailAddress(emailAddresses);
		sproutUser.addOAuthAccount(oauthAccount);
		sproutUser.setDisplayName(String.format("%s %s", firstName, lastName));
		return insert(sproutUser);
	}


	@Override
	public SproutUser insert(String username, String clearTextPassword, String firstName, String lastName) {
		return insert(username, clearTextPassword, firstName, lastName, Collections.<Role> emptySet());
	}

	@Override
	public SproutUser insert(String username, String clearTextPassword, String firstName, String lastName, Set<Role> authorities) {
		SproutUser user = new SproutUser(username, clearTextPassword, firstName, lastName, authorities);
		return insert(user);
	}
	
	@Override
	public SproutUser getOrInsertForOAuth(String firstName, String lastName, OAuthAccount oAuthAccount, Collection<EmailAddress> emailAddresses ){
		
		emailAddressRepository.findOrInsert(emailAddresses);
		Object[] emailStrings = emailAddresses.stream().map(e->e.getEmailAddress()).toArray();
		Query query = Query.query(Criteria.where("emailAddresses.emailAddress").in(emailStrings));
		SproutUser userDetails = mongoTemplate.findOne(query, SproutUser.class);
		
		if(userDetails == null){
			userDetails = insert(firstName, lastName, oAuthAccount, emailAddresses);
			mongoTemplate.save(userDetails);
		}
		return userDetails;
	}


	@Override
	public SproutUser updateMyProfile(SproutUser profile) {
		SproutUser userDetails = mongoTemplate.findOne(Query.query(Criteria.where("username").is(profile.getUsername())), SproutUser.class);
		userDetails.clearEmailAddresses();
		userDetails.addEmailAddress(profile.getEmailAddresses());
		userDetails.setDisplayName(profile.getDisplayName());
		userDetails.setFirstName(profile.getFirstName());
		userDetails.setLastName(profile.getLastName());
		userDetails.setPrimaryEmailAddress(profile.getPrimaryEmailAddress());
		mongoTemplate.save(userDetails);
		return userDetails;
	}
}
