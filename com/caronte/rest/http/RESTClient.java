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
import com.caronte.rest.exceptions.OperationExecutionException;

public class RESTClient 
{
	public static Object execute(String uri, MethodType method, ContentType contentType, CharsetType charsetType, Object content, HashMap<String, String> headers, ContentType produces) throws OperationExecutionException
	{
		HttpURLConnection connection = null;
		String mensaje = null;
		
		try
		{
			URL url = new URL(uri);
		    connection = (HttpURLConnection)url.openConnection();
		    connection.setRequestMethod(method.toString());
		    connection.setRequestProperty("Content-Type", contentType.toString() + (charsetType.toString() == null ? "" : ";" + charsetType.toString()));
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
		    
		    int responseCode = connection.getResponseCode();
		    
		    if(responseCode == 200 || responseCode == 401 || responseCode == 409)
		    {
	    		StringBuffer buffer = new StringBuffer();		    
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
	    		
	    		if (produces == ContentType.APPLICATION_JSON)
	    		{
		    		return JSON.parse(buffer.toString());
	    		}
	    		
	    		return buffer.toString();	    		
		    }
		}
		catch(Exception e)
		{
			mensaje = e.getMessage();
		}

		throw new OperationExecutionException("Error en servicio " + uri + (mensaje != null ? " : " +  mensaje : ""));
	}
	
	public static void main(String[] args) 
	{
		String uri;
		uri = "http://localhost:9090/mn-acl/client";

		HashMap<String, String> headers = new HashMap<String, String>();
		
		JSONObject jsonObject = new JSONObject();
		String json = "";
		
		json += "{\n";
		json += "\t\"p_clave_cliente\" : \"kio2\",\n";
		json += "\t\"p_contexto\" : \"kio2\",\n";
		json += "\t\"p_nombre_cliente\" : \"Fundacón KIO A.C.\",\n";
		json += "\t\"p_clave_usuario\" : \"RCONCHAMN\",\n";
		json += "\t\"p_password\" : \"RCONCHAMN\",\n";
		json += "\t\"p_nombre_usuario\" : \"Roberto\",\n";
		json += "\t\"p_apellidos\" : \"Concha Gallegos\",\n";
		json += "\t\"p_correo_electronico\" : \"roberto.concha@masnegocio.com\"\n";
		json += "}\n";
		
		headers.put("ProductoMN","EXPENSESCLOUD");
		
		try
		{
			jsonObject = JSON.parse(json);
			Object res = RESTClient.execute(uri, MethodType.POST, ContentType.APPLICATION_JSON, CharsetType.UTF_8, jsonObject, headers, ContentType.APPLICATION_JSON);
			System.out.println(res);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}
