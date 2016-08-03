package com.caronte.rest.exceptions;

import com.caronte.rest.interfaces.IHTTPStatusNotifiable;

public class AuthorizationException extends Exception implements IHTTPStatusNotifiable
{
	private static final long serialVersionUID = -3561417345404124452L;
	
	public Integer getHTTPStatus()
	{
		return 401;
	}
	
	public AuthorizationException(String message) 
	{
        super(message);
    }	
}
