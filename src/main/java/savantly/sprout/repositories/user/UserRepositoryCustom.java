package savantly.sprout.repositories.user;

import java.util.Collection;
import java.util.Set;

import savantly.sprout.domain.EmailAddress;
import savantly.sprout.domain.OAuthAccount;
import savantly.sprout.domain.SproutUser;
import savantly.sprout.security.Role;

public interface UserRepositoryCustom {
	SproutUser insert(SproutUser sproutUser);

	SproutUser insert(String username, String clearTextPassword, String firstName, String lastName);

	SproutUser insert(String username, String clearTextPassword, String firstName, String lastName,
			Set<Role> authorities);

	SproutUser insert(String firstName, String lastName, EmailAddress emailAddress);

	SproutUser getOrInsertForOAuth(String firstName, String lastName, OAuthAccount oAuthAccount,
			Collection<EmailAddress> emailAddress);

	SproutUser insert(String firstName, String lastName, OAuthAccount oauthAccount,
			Collection<EmailAddress> emailAddresses);

	SproutUser updateMyProfile(SproutUser myAccount);
}
