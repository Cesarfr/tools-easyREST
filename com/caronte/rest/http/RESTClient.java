package com.caronte.rest.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;

import com.caronte.json.JSON;
import com.caronte.json.JSONObject;
import com.caronte.rest.enums.CharsetType;
import com.caronte.rest.enums.ContentType;
import com.caronte.rest.enums.MethodType;
import com.caronte.rest.exceptions.AuthorizationException;
import com.caronte.rest.exceptions.OperationExecutionException;

public class RESTClient 
{
	public static Object execute(String uri, MethodType method, ContentType contentType, CharsetType charsetType, Object content, HashMap<String, String> headers, ContentType produces) throws OperationExecutionException, AuthorizationException
	{
		HttpURLConnection connection = null;
		String mensaje = null;
		StringBuffer buffer = new StringBuffer();		    
		int responseCode;
		JSONObject data = null;
		
		try
		{
			URL url = new URL(uri);
		    connection = (HttpURLConnection)url.openConnection();
		    connection.setRequestMethod(method.toString());
		    
		    String strContentType = contentType.toString() + (charsetType.toString() == null ? "" : ";charset=" + charsetType.toString());
		    
		    connection.setRequestProperty("Content-Type", strContentType);
		    connection.setRequestProperty("Accept-Charset", charsetType.toString());
		    connection.setUseCaches(false);
		    connection.setDoOutput(true);
		    
		    if (headers != null)
		    {
		    	for (Entry<String, String> entry : headers.entrySet()) 
		    	{
					connection.setRequestProperty(entry.getKey(), entry.getValue());
				}
		    }

		    if (content != null)
		    {
			    connection.setRequestProperty("Content-Length", Integer.toString(content.toString().length()));
			    Writer wr = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
			    String c = content.toString(); 
			    wr.write(c);
			    wr.flush();
			    wr.close();
		    }
		    
		    responseCode = connection.getResponseCode();
		    
		    if(responseCode == 200 || responseCode == 401 || responseCode == 409)
		    {
	    		BufferedReader reader;

	    		if (responseCode == 200)
	    		{
	    			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));	    			
	    		}
	    		else
	    		{
	    			reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));	    			
	    		}
	    		
	    		String linea;
	    		
	    		while((linea = reader.readLine()) != null)
	    		{
	    			buffer.append(linea);
	    		}
		    }
		    
			if (produces == ContentType.APPLICATION_JSON)
			{
	    		data = JSON.parse(buffer.toString());
			}
		}
		catch(Exception e)
		{
			throw new OperationExecutionException("Error en servicio " + uri + (mensaje != null ? " : " +  mensaje : ""));
		}
		    
		
		if (produces == ContentType.APPLICATION_JSON)
		{
			if (responseCode == 401)
			{
				throw new AuthorizationException("Error en servicio " + uri, data);
			}
			if (responseCode == 409)
			{
				throw new OperationExecutionException("Error en servicio " + uri, data);
			}

			return data;
		}
		else
		{
			if (responseCode == 401)
			{
				throw new AuthorizationException("Error en servicio " + uri, buffer.toString());
			}
			if (responseCode == 409)
			{
				throw new OperationExecutionException("Error en servicio " + uri, buffer.toString());
			}
			
			return buffer.toString();
		}
	}
}
