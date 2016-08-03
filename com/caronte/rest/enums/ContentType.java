package com.caronte.rest.enums;

public enum ContentType 
{
	NONE(null),
	APPLICATION_ATOM_XML("application/atom+xml"),
	APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
	APPLICATION_JSON("application/json"),
	APPLICATION_OCTET_STREAM("application/octet-stream"),
	APPLICATION_SVG_XML("application/svg+xml"),
	APPLICATION_XHTML_XML("application/xhtml+xml"),
	APPLICATION_XML("application/xml"),
	MULTIPART_FORM_DATA("multipart/form-data"),
	TEXT_HTML("text/html"),
	TEXT_PLAIN("text/plain"),
	TEXT_XML("text/xml"),
	IMAGE_PNG("image/png"),
	IMAGE_JPG("image/jpg");	

	private final String name;       

    private ContentType(String s) {
        name = s;
    }

    public String toString() {
       return this.name;
    }	
}
