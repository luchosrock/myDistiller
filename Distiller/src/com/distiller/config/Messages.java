package com.distiller.config;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static String BUNDLE_NAME = "";
	private static String CONFIG_DIR = "";
	private static String INPUT_DIR = "";
	private static String OUTPUT_DIR = "";

	private static ResourceBundle RESOURCE_BUNDLE = null;

	private Messages() {
	}
	
	public static void setConfigDir(String configDir) {
		if (configDir == null)
			configDir = "";
		if (configDir.endsWith(System.getProperty("file.separator")))
			configDir = configDir.substring(0, configDir.length()-1);
		CONFIG_DIR = configDir;
	}
	
	public static String getConfigDir() {
		return CONFIG_DIR;
	}
	
	public static void setInputDir(String inputDir) {
		if (inputDir == null)
			inputDir = "";
		if (inputDir.endsWith(System.getProperty("file.separator")))
			inputDir = inputDir.substring(0, inputDir.length()-1);
		INPUT_DIR = inputDir;
	}
	
	public static String getInputDir() {
		return INPUT_DIR;
	}
	
	public static void setOutputDir(String outputDir) {
		if (outputDir == null)
			outputDir = "";
		if (outputDir.endsWith(System.getProperty("file.separator")))
			outputDir = outputDir.substring(0, outputDir.length()-1);
		OUTPUT_DIR = outputDir;
	}
	
	public static String getOutputDir() {
		return OUTPUT_DIR;
	}
	
	public static void setLocale(String locale) {
		if (locale == null)
			locale = "es";
		if (locale.indexOf("_") > 0)
			locale = locale.substring(0, locale.indexOf("_"));
		BUNDLE_NAME = locale+".messages";
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	}
	
	public static String getLocale() {
		if (BUNDLE_NAME.length() == 0)
			return "";
		return BUNDLE_NAME.substring(0, BUNDLE_NAME.indexOf("."));
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
