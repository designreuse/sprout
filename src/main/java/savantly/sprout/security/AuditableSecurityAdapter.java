package savantly.sprout.security;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import savantly.sprout.domain.SproutUser;

public class AuditableSecurityAdapter<T extends 
			AbstractAuditableDomainObject<ID>, ID extends Serializable> implements AuditedDomainSecurity<T, ID>{
	
	final protected SproutUser getCurrentUser(){
		return (SproutUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	@Override
	@PreAuthorize("isFullyAuthenticated()")
	public boolean canCreate(T t) {
		return true;
	}
	
	@Override
	public boolean canList() {
		return true;
	}
	
	@Override
	@PreAuthorize("isFullyAuthenticated()")
	public boolean canUpdate(T t) {
		return t.getCreatedBy().equals(getCurrentUser());
	}
	
	@Override
	@PreAuthorize("isFullyAuthenticated()")
	public boolean canDelete(T t) {
		return t.getCreatedBy().equals(getCurrentUser());
	}

	@Override
	public T filter(T t) {
		return t;
	}

	@Override
	public Page<T> filter(Page<T> page) {
		return page;
	}

	@Override
	public List<T> filter(List<T> list) {
		return list;
	}

}