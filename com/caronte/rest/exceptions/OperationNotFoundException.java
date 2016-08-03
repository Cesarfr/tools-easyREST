package com.caronte.rest.exceptions;

import com.caronte.rest.interfaces.IHTTPStatusNotifiable;

public class OperationNotFoundException extends Exception implements IHTTPStatusNotifiable 
{
	private static final long serialVersionUID = -2026030435893464724L;

	public Integer getHTTPStatus()
	{
		return 405;
	}
	
	public OperationNotFoundException(String message) 
	{
        super(message);
    }	
}
