package com.caronte.rest.exceptions;

import com.caronte.rest.interfaces.IHTTPStatusNotifiable;

public class OperationExecutionException extends Exception implements IHTTPStatusNotifiable
{
	private static final long serialVersionUID = -6943756540282297834L;
	private Object data;

	public OperationExecutionException(String message, Object data) {
		super(message);
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}
	
	public Integer getHTTPStatus()
	{
		return 409;
	}
	
	public OperationExecutionException(String message) 
	{
		super(message);
	}
}
