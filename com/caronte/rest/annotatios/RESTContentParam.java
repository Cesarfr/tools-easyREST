package com.caronte.rest.annotatios;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.caronte.rest.enums.ContentParamType;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RESTContentParam
{
	public ContentParamType value() default ContentParamType.TEXT;
}
