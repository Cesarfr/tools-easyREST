package com.caronte.rest;

import com.caronte.rest.enums.CharsetType;
import com.caronte.rest.enums.ContentType;
import com.caronte.rest.enums.MethodType;

class RESTMethodReference 
{
	private String path;
	private String className;
	private String method;
	private ContentType contentType;
	private ContentType produces;
	private CharsetType producesCharset;
	private MethodType methodType;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public ContentType getContentType() {
		return contentType;
	}
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	public ContentType getProduces() {
		return produces;
	}
	public void setProduces(ContentType produces) {
		this.produces = produces;
	}
	public CharsetType getProducesCharset() {
		return producesCharset;
	}
	public void setProducesCharset(CharsetType producesCharset) {
		this.producesCharset = producesCharset;
	}
	public MethodType getMethodType() {
		return methodType;
	}
	public void setMethodType(MethodType methodType) {
		this.methodType = methodType;
	}
}
