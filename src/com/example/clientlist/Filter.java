package com.example.clientlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


//it is a rule to include or exclude applications according to their package names
public class Filter implements Serializable {
	private static final long serialVersionUID = -2518820183367987812L;

	public final static String DELIMITER = ";";
	
	private String name;
	private List<String> keywordsInclude = new ArrayList<String>();
	private List<String> keywordsExclude = new ArrayList<String>();
	
	
	public Filter(String name, String includeString, String excludeString) {
		this.name = name;
		//split the delimited strings into list of keywords
		this.keywordsInclude = new ArrayList<String>(
				Arrays.asList(
						includeString.split(DELIMITER)));
		this.keywordsExclude = new ArrayList<String>(
				Arrays.asList(
						excludeString.split(DELIMITER)));
	}
	
	public Filter(String name, List<String> keywordsInclude, List<String> keywordsExclude) {
		this.name = name;
		this.keywordsInclude = keywordsInclude;
		this.keywordsExclude = keywordsExclude;
				
	}
	
	public Filter() {
		// TODO: filter name and one of keywords are obligatory, make a check
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getKeywordsInclude() {
		return keywordsInclude;
	}
	public void setKeywordsInclude(List<String> keywordsInclude) {
		this.keywordsInclude = keywordsInclude;
	}
	public List<String> getKeywordsExclude() {
		return keywordsExclude;
	}
	public void setKeywordsExclude(List<String> keywordsExclude) {
		this.keywordsExclude = keywordsExclude;
	}
	
	

}
