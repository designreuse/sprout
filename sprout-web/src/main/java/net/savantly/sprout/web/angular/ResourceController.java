package net.savantly.sprout.web.angular;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
import org.springframework.web.bind.annotation.RequestParam;

import net.savantly.sprout.domain.user.SproutUser;
import net.savantly.sprout.exceptions.UnauthorizedClientException;
import net.savantly.sprout.mongo.repository.ExtendedMongoRepository;
import net.savantly.sprout.security.AuditedDomainSecurity;
import net.savantly.sprout.security.Roles;
import net.savantly.sprout.security.SproutAuditable;

@SuppressWarnings("rawtypes")
public abstract class ResourceController<T extends SproutAuditable<ID>, ID extends Serializable, R extends ExtendedMongoRepository> {
	
	@Autowired(required=false)
	AuditedDomainSecurity<T, ID> domainSecurity;
	
	private R entityRepository;
	
	public ResourceController(R entityRepository) {
		this.entityRepository = entityRepository;
	}

	/**
	 * Creates a new Entity in storage
	 * @param model
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value={"", "/{id}"},method = RequestMethod.POST)
	public T create(@RequestBody @Valid T model, @AuthenticationPrincipal SproutUser user) {
		if(!canCreate(model, user)) {
			throw new UnauthorizedClientException("You do not have authorization to create new items.");
		}
		ResourceEvent<T> resourceEvent = onCreating(new ResourceEvent<T>(model, false));
		if(!resourceEvent.isHandled()){
			T result = (T) this.entityRepository.insert(resourceEvent.getEntity());
			onCreated(model);
			return result;
		}
		else return resourceEvent.getEntity();
		
	}
	
	/**
	 * Search for entities where 'field' contains 'value'
	 * 
	 * @param field
	 * @param value
	 * @param pageable
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value={"/search/{field}/{value}"},method = RequestMethod.GET)
	public Page<T> search(@PathVariable String field, @PathVariable String value, @RequestParam(required=false) Pageable pageable, @AuthenticationPrincipal SproutUser user) {
		Query query = Query.query(Criteria.where(field).is(value));
		if(pageable == null){
			pageable = new PageRequest(0, 20);
		}
		return this.entityRepository.query(query, pageable);
	}

	/**
	 * Get a list of all the entities
	 * 
	 * @param user
	 * @param pageable 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public List<T> list(Pageable pageable, @AuthenticationPrincipal SproutUser user) {
		if(!canList(user))throw new UnauthorizedClientException("You do not have authorization to query items.");
		return this.entityRepository.findAll(pageable).getContent();
	}
	
	/**
	 * Get a page of all the entities
	 * 
	 * @param user
	 * @param pageable 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/page", method = RequestMethod.GET)
	public Page<T> page(Pageable pageable, @AuthenticationPrincipal SproutUser user) {
		if(!canList(user))throw new UnauthorizedClientException("You do not have authorization to query items.");
		return this.entityRepository.findAll(pageable);
	}
	

	/**
	 * Get one entity by the ID
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public T get(@PathVariable("id") ID id, @AuthenticationPrincipal SproutUser user) {
		if(!canGet(id, user))throw new UnauthorizedClientException("You do not have authorization to query this item.");
		T result = (T) this.entityRepository.findOne(id);
		return onFindOne(result);
	}

	/**
	 * Update an entity
	 * 
	 * @param id
	 * @param model
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{id}", method = {RequestMethod.PUT})
	public T update(@PathVariable("id") ID id, @RequestBody @Valid T model, @AuthenticationPrincipal SproutUser user) {
		T existingModel = (T) this.entityRepository.findOne(model.getId());
		if(!canUpdate(existingModel, user))throw new UnauthorizedClientException("You do not have authorization to update this item.");
		ResourceEvent<T> resourceEvent = onUpdating(new ResourceEvent<T>(model, false));
		if(!resourceEvent.isHandled()){
			T result = (T) this.entityRepository.save(resourceEvent.getEntity());
			onUpdated(result);
			return result;
		}
		else return resourceEvent.getEntity();
	}

	/**
	 * Delete an entity 
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Boolean> delete(@PathVariable("id") ID id, @AuthenticationPrincipal SproutUser user) {
		if(!canDelete(id, user))throw new UnauthorizedClientException("You do not have authorization to delete this item.");
		this.entityRepository.delete(id);
		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}
	
	protected final boolean isUserInRole(String role){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null) return false;
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (role.equals(authority.getAuthority())) return true;
		}
		return false;
	}
	
	protected final boolean isUserInRole(Roles... roles) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null) return false;
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			for (Roles role : roles) {
				if (role.name().equals(authority.getAuthority())) return true;
			}
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
	protected boolean canCreate(T model, SproutUser currentUser) {
		if(null != domainSecurity){
			return domainSecurity.canCreate(model);
		} else
		return true;
	}
	
	protected boolean canList(SproutUser currentUser) {
		if(null != domainSecurity){
			return domainSecurity.canList();
		} else
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean canGet(ID id, SproutUser currentUser) {
		if(null != domainSecurity){
			return domainSecurity.filter((T)entityRepository.findOne(id)) != null;
		} else
		return true;
	}
	
	/**
	 * 
	 * @param model The Model to be persisted
	 * @param user The current user
	 * @return True or false if the model should be updated
	 */
	protected boolean canUpdate(T model, SproutUser currentUser) {
		if(null != domainSecurity){
			return domainSecurity.canUpdate(model);
		} else
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean canDelete(ID id, SproutUser currentUser) {
		if(null != domainSecurity){
			return domainSecurity.canDelete((T)entityRepository.findOne(id));
		} else
		return true;
	}

	protected R getEntityRepository() {
		return entityRepository;
	}
}