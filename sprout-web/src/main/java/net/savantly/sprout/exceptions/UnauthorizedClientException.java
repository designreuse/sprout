package net.savantly.sprout.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Not Authorized") 
public class UnauthorizedClientException extends RuntimeException {

	public UnauthorizedClientException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedClientException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedClientException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedClientException(String string) {
		// TODO Auto-generated constructor stub
		super(string);
	}

	private static final long serialVersionUID = 6991775140492674768L;

}
