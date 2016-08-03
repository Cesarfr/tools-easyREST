package com.caronte.rest.exceptions;

import com.caronte.rest.interfaces.IHTTPStatusNotifiable;

public class ContentException extends Exception implements IHTTPStatusNotifiable 
{
	private static final long serialVersionUID = 8197739982128153563L;

	public Integer getHTTPStatus()
	{
		return 400;
	}
	
	public ContentException(String message) 
	{
		super(message);
	}
}
