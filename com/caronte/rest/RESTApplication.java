package com.caronte.rest;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caronte.jpath.JPATH;
import com.caronte.json.JSON;
import com.caronte.json.JSONObject;
import com.caronte.json.JSONValue;
import com.caronte.rest.annotatios.RESTContentParam;
import com.caronte.rest.annotatios.RESTController;
import com.caronte.rest.annotatios.RESTHeaderParam;
import com.caronte.rest.annotatios.RESTMethod;
import com.caronte.rest.annotatios.RESTMethods;
import com.caronte.rest.annotatios.RESTPathParam;
import com.caronte.rest.annotatios.RESTURLParam;
import com.caronte.rest.enums.CharsetType;
import com.caronte.rest.enums.ContentParamType;
import com.caronte.rest.enums.ContentType;
import com.caronte.rest.enums.MessageType;
import com.caronte.rest.enums.MethodType;
import com.caronte.rest.exceptions.AuthorizationException;
import com.caronte.rest.exceptions.ContentException;
import com.caronte.rest.exceptions.OperationExecutionException;
import com.caronte.rest.exceptions.OperationNotFoundException;
import com.caronte.rest.interfaces.IHTTPStatusNotifiable;
import com.caronte.rest.model.Execution;

@WebServlet(value="/*", name="api-rest-application")
public class RESTApplication extends HttpServlet
{
	private static final long serialVersionUID = 478998679743033075L;
	private static List<RESTMethodReference> methodReferenceList;
	private static HashMap<String, MethodType> methodTypeMap;
	private static HashMap<String, ContentType> contentTypeMap;
	
	static
	{
		contentTypeMap = new HashMap<String, ContentType>();
		methodTypeMap = new HashMap<String, MethodType>();
		
		contentTypeMap.put("application/atom+xml", ContentType.APPLICATION_ATOM_XML);
		contentTypeMap.put("application/x-www-form-urlencoded", ContentType.APPLICATION_FORM_URLENCODED);
		contentTypeMap.put("application/json", ContentType.APPLICATION_JSON);
		contentTypeMap.put("application/octet-stream", ContentType.APPLICATION_OCTET_STREAM);
		contentTypeMap.put("application/svg+xml", ContentType.APPLICATION_SVG_XML);
		contentTypeMap.put("application/xhtml+xml", ContentType.APPLICATION_XHTML_XML);
		contentTypeMap.put("application/xml", ContentType.APPLICATION_XML);
		contentTypeMap.put("multipart/form-data", ContentType.MULTIPART_FORM_DATA);
		contentTypeMap.put("text/html", ContentType.TEXT_HTML);
		contentTypeMap.put("text/plain", ContentType.TEXT_PLAIN);
		contentTypeMap.put("text/xml", ContentType.TEXT_XML);
		contentTypeMap.put("image/png", ContentType.IMAGE_PNG);
		contentTypeMap.put("image/jpg", ContentType.IMAGE_JPG);
		
		methodTypeMap.put("GET", MethodType.GET);
		methodTypeMap.put("POST", MethodType.POST);
		methodTypeMap.put("PUT", MethodType.PUT);
		methodTypeMap.put("DELETE", MethodType.DELETE);
		methodTypeMap.put("OPTION", MethodType.OPTION);
		methodTypeMap.put("TRACE", MethodType.TRACE);
	}
	
	private void getAllClasses(ServletContext servletContext, String path)
	{
		Set<String> resourcePaths = servletContext.getResourcePaths(path);
		
		if (resourcePaths != null)
		{
			for (String resourcePath : resourcePaths) 
			{
				File file = new File(servletContext.getRealPath("/") + resourcePath);
				
				if (file.isDirectory())
				{
					getAllClasses(servletContext, resourcePath);
				}
				else
				{				
					if (file.isFile() && resourcePath.endsWith(".class"))
					{
						String className = resourcePath.replace(".class", "").replace("/WEB-INF/classes/", "").replaceAll("/", ".");
						
						try
						{
							Annotation[] annotations = Class.forName(className).getAnnotations();
							
							for (Annotation annotation : annotations) 
							{
								if (annotation instanceof RESTController)
								{								
									String controllerPath = ((RESTController) annotation).value();
									
									Method[] methods = Class.forName(className).getMethods();
									
									for (Method method : methods) 
									{
										Annotation[] methodAnnotations = method.getAnnotations();
										
										for (Annotation methodAnnotation : methodAnnotations) 
										{
											Annotation[] arrayAnnotations = null;
											
											if (methodAnnotation instanceof RESTMethod)
											{
												arrayAnnotations = new Annotation[1];
												arrayAnnotations[0] = methodAnnotation;
											}
											
											if (methodAnnotation instanceof RESTMethods)
											{
												arrayAnnotations = ((RESTMethods) methodAnnotation).value();
											}
											
											if (arrayAnnotations != null)
											{
												for (Annotation a : arrayAnnotations) 
												{
													String methodPath = ((RESTMethod) a).path();
													ContentType contentType = ((RESTMethod) a).contentType();
													ContentType produces = ((RESTMethod) a).produces();
													CharsetType producesCharset = ((RESTMethod) a).producesCharset();
													MethodType methodType = ((RESTMethod) a).method();

													RESTMethodReference restMethodReference = new RESTMethodReference();
													
													restMethodReference.setClassName(className);
													restMethodReference.setMethod(method.getName());
													restMethodReference.setPath(controllerPath + methodPath);
													restMethodReference.setMethodType(methodType);
													restMethodReference.setContentType(contentType);
													restMethodReference.setProduces(produces);
													restMethodReference.setProducesCharset(producesCharset);
													
													methodReferenceList.add(restMethodReference);
												}
											}
										}
									}
								}
							}
						}
						catch(Exception e)
						{
						}
					}
				}
			}
		}
	}
	
	private String getDynamicPath(String annotationPath, String urlPath, HashMap<String, String> pathParamMap)
	{
		String [] annotationArrayPath = annotationPath.split("/");
		String [] urlArrayPath = urlPath.split("/");
		StringBuffer stringBuffer = new StringBuffer();
		String result = annotationPath;
		
		if (annotationArrayPath.length == urlArrayPath.length)
		{
			for(int i = 0; i < annotationArrayPath.length; i++)
			{
				String annotationElement = annotationArrayPath[i];
				
				if (annotationElement.trim().length() == 0)
				{
					continue;
				}

				if (annotationElement.startsWith("{") && annotationElement.endsWith("}"))
				{
					stringBuffer.append("/" + urlArrayPath[i]);
					pathParamMap.put(annotationElement.substring(1, annotationElement.length() - 1), urlArrayPath[i]);
				}
				else
				{
					stringBuffer.append("/" + annotationElement);
				}
			}

			result = stringBuffer.toString();
		}
		
		return result;
	}
	
	private void findMethod(String path, MethodType methodType, ContentType contentType, HashMap<String, String> pathParamMap, Execution execution)
	{
		execution.setExecutionMethod(null);
		execution.setExecutionObject(null);
		execution.setExecutionProduces(null);
		execution.setExecutionProducesCharset(null);
		
		if (methodType != null && path != null)
		{
			try
			{
				for (RESTMethodReference restMethodReference : methodReferenceList) 
				{
					String dynamicPath = getDynamicPath(restMethodReference.getPath(), path, pathParamMap);
					
					if (restMethodReference.getMethodType() == methodType && dynamicPath.equals(path) && restMethodReference.getContentType() == contentType)
					{
						Class<?> clazz = Class.forName(restMethodReference.getClassName());
						Method[] methods = clazz.getMethods();
						
						execution.setExecutionObject(clazz.newInstance());

						for (Method method : methods) 
						{
							if (method.getName().equals(restMethodReference.getMethod()))
							{
								execution.setExecutionMethod(method);
								execution.setExecutionProduces(restMethodReference.getProduces());
								execution.setExecutionProducesCharset(restMethodReference.getProducesCharset());
								return;
							}
						}
					}
				}
			}
			catch(Exception e)
			{
			}
		}
	}
	
	private Object executeMethod(String content, HashMap<String, String> parameters, HashMap<String, String> headers, HashMap<String, String> pathParamMap, String contentType, Execution execution) throws AuthorizationException, OperationNotFoundException, ContentException, OperationExecutionException	
	{
		if (execution.getExecutionMethod() == null || execution.getExecutionObject() == null || execution.getExecutionProduces() == null)
		{
			throw new OperationNotFoundException("Unsupported operation");
		}
		
		Parameter[] restMethodParameters = execution.getExecutionMethod().getParameters();
		Object[] parametersRestMethod = null;
		int ip = 0;
		
		if (restMethodParameters != null && restMethodParameters.length > 0)
		{
			parametersRestMethod = new Object[restMethodParameters.length];
					
			for (Parameter restMethodParameter : restMethodParameters) 
			{
				parametersRestMethod[ip] = null;
				
				Annotation[] restMethodParameterAnnotations = restMethodParameter.getAnnotations();
				
				for (Annotation restMethodParameterAnnotation : restMethodParameterAnnotations) 
				{
					if (restMethodParameterAnnotation instanceof RESTContentParam)
					{
						if (content == null)
						{
							parametersRestMethod[ip] = null;
						}
						else
						{
							if (((RESTContentParam) restMethodParameterAnnotation).value() == ContentParamType.JSON)
							{
								try
								{
									parametersRestMethod[ip] = JSON.parse(content);
								}
								catch(Exception e)
								{
									throw new ContentException(e.getMessage());
								}
							}
							if (((RESTContentParam) restMethodParameterAnnotation).value() == ContentParamType.TEXT)
							{
								parametersRestMethod[ip] = content;
							}
						}
					}
					if (restMethodParameterAnnotation instanceof RESTHeaderParam)
					{
						parametersRestMethod[ip] = headers.get(((RESTHeaderParam) restMethodParameterAnnotation).value().toLowerCase());
					}
					if (restMethodParameterAnnotation instanceof RESTURLParam)
					{
						parametersRestMethod[ip] = parameters.get(((RESTHeaderParam) restMethodParameterAnnotation).value().toLowerCase());
					}
					if (restMethodParameterAnnotation instanceof RESTPathParam)
					{
						parametersRestMethod[ip] = pathParamMap.get(((RESTPathParam)restMethodParameterAnnotation).value());
					}
				}
				
				ip++;
			}
		}
		
		try
		{
			Object executionReturn = execution.getExecutionMethod().invoke(execution.getExecutionObject(), parametersRestMethod);
			
			if (execution.getExecutionProduces() == ContentType.APPLICATION_JSON)
			{
				JSONObject jsonObject = (JSONObject)executionReturn;
				JSONValue jsonValue = JPATH.find(jsonObject, "/message");
				String message = jsonValue == null ? "" : jsonValue.getValue().toString();
				JPATH.remove(jsonObject, "/message");
				
				if (!jsonObject.elements().hasMoreElements())
				{
					jsonObject = null;
				}
				
				executionReturn = createJSONMessage(200, MessageType.SUCCESS, message, jsonObject);
			}
			
			return executionReturn;
		}
		catch(InvocationTargetException | IllegalAccessException | IllegalArgumentException e) 
		{
			if (e instanceof InvocationTargetException)
			{
				if (((InvocationTargetException) e).getTargetException() != null)
				{					
					if (((InvocationTargetException) e).getTargetException() instanceof AuthorizationException)
					{
						throw new AuthorizationException(((InvocationTargetException) e).getTargetException().getMessage());
					}
					else
					{
						throw new OperationExecutionException(((InvocationTargetException) e).getTargetException().toString());
					}
				}
				else
				{
					throw new OperationExecutionException(e.getMessage());
				}
			}
		}
		
		return null;
	}
	
	public static JSONObject createJSONMessage(Integer code, MessageType type, String message, JSONObject data)
	{
		JSONObject jsonObject = new JSONObject();

		try
		{
			jsonObject.addPair("code", code);
			jsonObject.addPair("type", type.toString());
			jsonObject.addPair("message", message);
			
			if (data != null)
			{
				jsonObject.addPair("data", data);
			}
		}
		catch(Exception e)
		{				
		}
		
		return jsonObject;
	}
	
	public static String createTextMessage(Integer code, MessageType type, String message)
	{
		return "[" + type.toString() + "] " + code.toString() + " : " + message;
	}

	@Override
	protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException 
	{
		Integer contengLength = servletRequest.getContentLength();
		String content = null;
		String path = servletRequest.getPathInfo();
		HashMap<String, String> parameters = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> pathParamMap = new HashMap<String, String>();
		Execution execution = new Execution();
		
		String characterEncoding = servletRequest.getCharacterEncoding() == null ? "US-ASCII" : servletRequest.getCharacterEncoding();
		String method = servletRequest.getMethod() == null ? "GET" : servletRequest.getMethod();
		String contentType = servletRequest.getContentType() == null ? "text/plain" : servletRequest.getContentType();
		
		if (contentType.contains(";"))
		{
			contentType = contentType.substring(0, contentType.indexOf(";")).trim();					
		}

		if (contengLength > 0)
		{
			ServletInputStream inputStream = servletRequest.getInputStream();
			
			int c;
			int i = 0;
			byte[] bytes = new byte[contengLength];		
			
			while((c = inputStream.read()) != -1)
			{
				bytes[i++] = (byte)c;
			}
			
			content = new String(bytes, characterEncoding);
		}
		
		Enumeration<String> parameterNames = servletRequest.getParameterNames();

		while (parameterNames.hasMoreElements())
		{
			String parameterName = parameterNames.nextElement();
			String parameterValue = servletRequest.getParameter(parameterName);
			parameters.put(parameterName, parameterValue);
		}		
		
		Enumeration<String> headerNames = servletRequest.getHeaderNames();
		
		while (headerNames.hasMoreElements())
		{
			String headerName = headerNames.nextElement().toLowerCase();
			String headerValue = servletRequest.getHeader(headerName);
			headers.put(headerName, headerValue);
		}		
		
		if (methodReferenceList == null)
		{
			methodReferenceList = new ArrayList<RESTMethodReference>();
			getAllClasses(servletRequest.getServletContext(), "/WEB-INF/classes");
		}
		
		findMethod(path, methodTypeMap.get(method), contentTypeMap.get(contentType), pathParamMap, execution);
		
		try
		{
			servletResponse.setStatus(200);
			servletResponse.setContentType(execution.getExecutionProduces().toString());
			servletResponse.setCharacterEncoding(execution.getExecutionProducesCharset().toString());

			if (execution.getExecutionProduces() != null)
			{
				switch (execution.getExecutionProduces()) 
				{
				case IMAGE_JPG:
				case IMAGE_PNG:
					byte[] bytes = (byte[])executeMethod(content, parameters, headers, pathParamMap, contentType, execution);
					servletResponse.getOutputStream().write(bytes);
					servletResponse.getOutputStream().flush();
					break;
				default:
					servletResponse.getWriter().print(executeMethod(content, parameters, headers, pathParamMap, contentType, execution));
					servletResponse.getWriter().flush();
					break;
				}
			}
			else
			{
				servletResponse.getWriter().print(executeMethod(content, parameters, headers, pathParamMap ,contentType, execution));
				servletResponse.getWriter().flush();
			}
		}
		catch (OperationNotFoundException | OperationExecutionException | ContentException | AuthorizationException e) 
		{
			servletResponse.setStatus(((IHTTPStatusNotifiable)e).getHTTPStatus());
			
			if (execution.getExecutionProduces() == ContentType.APPLICATION_JSON)
			{
				servletResponse.getWriter().print(createJSONMessage(((IHTTPStatusNotifiable)e).getHTTPStatus(), MessageType.ERROR, e.getMessage(), null));
				servletResponse.getWriter().flush();
			}
			else
			{
				servletResponse.getWriter().print(createTextMessage(((IHTTPStatusNotifiable)e).getHTTPStatus(), MessageType.ERROR, e.getMessage()));
				servletResponse.getWriter().flush();
			}
		}

	}
}
