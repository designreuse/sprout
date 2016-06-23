package savantly.sprout.repositories.user;

import java.util.List;

import savantly.sprout.domain.EmailAddress;

public class ProfileProjectionImpl implements ProfileProjection{
	

	private EmailAddress primaryEmailAddress;
	private String username;
	private String displayName;
	private String gravatarUrl;
	private List<EmailAddress> emailAddresses;

	@Override
	public EmailAddress getPrimaryEmailAddress() {
		return primaryEmailAddress;
	}
	
	@Override
	public List<EmailAddress> getEmailAddresses() {
		return emailAddresses;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getGravatarUrl() {
		return gravatarUrl;
	}


}
