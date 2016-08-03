package com.caronte.rest.annotatios;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.caronte.rest.enums.CharsetType;
import com.caronte.rest.enums.ContentType;
import com.caronte.rest.enums.MethodType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RESTMethods.class)
public @interface RESTMethod 
{
	public ContentType contentType() default ContentType.TEXT_PLAIN;
	public ContentType produces() default ContentType.TEXT_PLAIN;
	public MethodType method() default MethodType.GET;
	public CharsetType producesCharset() default CharsetType.NONE;
	public String path() default "";
}
