package com.distiller.docs;

import java.util.Vector;

/**
 * PatternSet is a wrapper class that facilitates the management of patterns. 
 * Each pattern type requires one instance of PatternSet. 
 * 
 * @author Kurt Englmeier
 *
 */
public class PatternSet {
	private Vector<String> singularNames = new Vector<String>();
	private Vector<String> pluralNames = new Vector<String>();
	private Vector<String> patterns = new Vector<String>();
	
	/**
	 * The constructor takes the list of string entries 
	 * @param entries Vector&#60;String&#62; patterns as string entries
	 */
	public PatternSet(Vector<String> entries) {
		for (String entry : entries) {
			String pattern = null;
			String pName = null;
			String sName = null;
			if ((entry.trim().length() > 0) && (!entry.startsWith("#") && (entry.indexOf("=") > 0))) {
				pattern = entry.substring(entry.indexOf("=")+1);
				String names = entry.substring(0, entry.indexOf("="));
				String[] parts = names.split(",");
				sName = parts[0];
				if (parts.length > 1)
					pName = parts[1];
				if ((pattern != null) && (sName != null)) {
					singularNames.addElement(sName);
					pluralNames.addElement(pName);
					patterns.addElement(pattern);
				}
			}
		}
	}
	
	/**
	 * checks if a particular pattern exists under the specified "name" in its plural or singular form.
	 *  
	 * @param name String - search term
	 * @return true if there is a pattern that corresponds to this search term, otherwise false.
	 */
	public boolean contains(String name) {
		return singularNames.contains(name) || pluralNames.contains(name);
	}
	
	/**
	 * checks if the search term corresponds to a plural form of a pattern's identifier.
	 * 
	 * @param name String - search term
	 * @return true if search term corresponds to the plural form of a pattern's identifier.
	 */
	public boolean isPlural(String name) {
		return (pluralNames.contains(name));
	}
	
	/**
	 * Returns the pattern that corresponds to the search term. 
	 * @param name String - search term
	 * @return pattern
	 */
	public String getPattern(String name) {
		int index = -1;
		if (singularNames.contains(name)) {
			index = singularNames.indexOf(name);
		} else if (pluralNames.contains(name)) {
			index = pluralNames.indexOf(name);
		}
		if (index == -1)
			return null;
		return patterns.elementAt(index);
	}
	
	/**
	 * Returns the pattern from the set at a particular index.
	 * @param index index of the pattern
	 * @return pattern
	 */
	public String getPattern(int index) {
		if ((index >= 0) && (index < patterns.size())) {
			return patterns.elementAt(index);
		}
		return null;
	}
	
	/**
	 * The singular form of the pattern's identifier serves as standard name. 
	 * In the end, this method returns the singular form that corresponds to the 
	 * search term. 
	 * 
	 * @param name String - search term
	 * @return standard (singular) form of the search term
	 */
	public String getStandardName(String name) {
		int index = -1;
		if (pluralNames.contains(name)) {
			index = pluralNames.indexOf(name);
		}
		if (index == -1)
			return name;
		return singularNames.elementAt(index);
	}

	/**
	 * The singular form of the pattern's identifier serves as standard name. 
	 * In the end, this method returns the singular form that corresponds to the 
	 * search term. 
	 * 
	 * @param index index of the pattern
	 * @return standard (singular) form of the search term
	 */
	public String getStandardName(int index) {
		if ((index >= 0) && (index < singularNames.size())) {
			return singularNames.elementAt(index);
		}
		return null;
	}
	
	/**
	 * Returns the plural form of the pattern's identifier that corresponds to search term
	 * @param name String - search term
	 * @return plural form the search term. If there is no plural form the method returns null.
	 */
	public String getPluralName(String name) {
		int index = -1;
		if (singularNames.contains(name)) {
			index = singularNames.indexOf(name);
		}
		if (index == -1)
			return null;
		return pluralNames.elementAt(index);
	}
	
	/**
	 * Returns the size of the pattern collection.
	 * @return size of the pattern collection.
	 */
	public int size() {
		return singularNames.size();
	}
}
