package savantly.sprout.web.angular;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Auditable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import savantly.sprout.domain.SproutUser;
import savantly.sprout.exceptions.UnauthorizedClientException;
import savantly.sprout.web.security.Roles;

public class ResourceController<T extends Auditable<SproutUser, ID>, ID extends Serializable> {
	
	private MongoRepository<T, ID> entityRepository;

	public ResourceController(MongoRepository<T, ID> entityRepository) {
		this.entityRepository = entityRepository;
	}

	@RequestMapping(value={"", "/{id}"},method = RequestMethod.POST)
	
	public T create(@RequestBody @Valid T model, @AuthenticationPrincipal SproutUser user) {
		if(!canCreate(model, user)) {
			throw new RuntimeException("You do not have authorization to create new items.");
		}
		ResourceEvent<T> resourceEvent = onCreating(new ResourceEvent<T>(model, false));
		if(!resourceEvent.isHandled()){
			T result = this.entityRepository.insert(resourceEvent.getEntity());
			onCreated(model);
			return result;
		}
		else return resourceEvent.getEntity();
		
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<T> list(@AuthenticationPrincipal SproutUser user) {
		if(!canList(user))throw new UnauthorizedClientException("You do not have authorization to query items.");
		return this.entityRepository.findAll();
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public T get(@PathVariable("id") ID id, @AuthenticationPrincipal SproutUser user) {
		if(!canGet(id, user))throw new UnauthorizedClientException("You do not have authorization to query this item.");
		T result = this.entityRepository.findOne(id);
		return onFindOne(result);
	}

	@RequestMapping(value = "/{id}", method = {RequestMethod.PUT})
	public T update(@PathVariable("id") ID id, @RequestBody @Valid T model, @AuthenticationPrincipal SproutUser user) {
		if(!canUpdate(model, user))throw new UnauthorizedClientException("You do not have authorization to update this item.");
		ResourceEvent<T> resourceEvent = onUpdating(new ResourceEvent<T>(model, false));
		if(!resourceEvent.isHandled()){
			T result = this.entityRepository.save(resourceEvent.getEntity());
			onUpdated(model);
			return result;
		}
		else return resourceEvent.getEntity();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Boolean> delete(@PathVariable("id") ID id, @AuthenticationPrincipal SproutUser user) {
		if(!canDelete(id, user))throw new UnauthorizedClientException("You do not have authorization to delete this item.");
		this.entityRepository.delete(id);
		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}
	
	protected final boolean  isUserInRole(String role){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null) return false;
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (role.equals(authority.getAuthority())) return true;
		}
		return false;
	}
	
	protected final boolean isUserInRole(Roles role) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null) return false;
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (role.name().equals(authority.getAuthority())) return true;
		}
		return false;
	}
	
	protected ResourceEvent<T> onCreating(ResourceEvent<T> resourceEvent){
		return resourceEvent;
	}
	
	protected void onCreated(T model) {
	}
	
	protected ResourceEvent<T> onUpdating(ResourceEvent<T> resourceEvent){
		return resourceEvent;
	}
	
	protected void onUpdated(T model) {
	}
	
	protected T onFindOne(T result) {
		return result;
	}
	
	/**
	 * 
	 * @param model The Model to be persisted
	 * @param user The current user
	 * @return True or false if the model should be created
	 */
	protected boolean canCreate(T model, SproutUser user) {
		return true;
	}
	
	protected boolean canList(SproutUser user) {
		return true;
	}
	
	protected boolean canGet(ID id, SproutUser user) {
		return true;
	}
	
	/**
	 * 
	 * @param model The Model to be persisted
	 * @param user The current user
	 * @return True or false if the model should be updated
	 */
	protected boolean canUpdate(T model, SproutUser user) {
		return true;
	}
	
	protected boolean canDelete(ID id, SproutUser user) {
		return true;
	}
}