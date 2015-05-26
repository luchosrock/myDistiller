package com.distiller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;

import com.distiller.annotation.AnnotationManager;
import com.distiller.docs.Document;
import com.distiller.docs.PatternSet;
import com.distiller.web.Status;

public class Distiller {
	
	private static String userDir;
	private static HashMap<String, Vector<Document>> documentRegistry = null;
	private static HashMap<String, String> identifierRegistry = null;
	private static Vector<String> wordlist_numbers = null;
	private static Vector<String> wordlist_months = null;
	private static Vector<String> wordlist_days = null;
	private static Vector<String> extensions = null;
	private static Vector<String> connectorPatternStrings = null;
	private static Vector<String> wordPatternStrings = null;
	private static Vector<String> basicNamedEntitiesStrings = null;
	private static Vector<String> namedEntitiesStrings = null;
	private static Vector<String> locationBasedVariablesStrings = null;
	
	/**
	 * Sets the basic configuration parameter for Distiller. It expects to find the configuration file Distiller4.config (UTF-8) in its user path.
	 * If the configuration file is absent you have to specify explicitly these parameters.
	 * @param inputPath		location of the documents to be processed
	 * @param outputPath	location of the processed (annotated) files
	 * @param configPath	location of the specific configuration data for distiller, like names of months, patterns, etc.
	 */
	protected static void setConfiguration(String inputPath, String outputPath, String configPath, String localeString) {
		String line; String[] parms;
		com.distiller.config.Messages.setConfigDir(configPath);
		com.distiller.config.Messages.setInputDir(inputPath);
		com.distiller.config.Messages.setOutputDir(outputPath);
		com.distiller.config.Messages.setLocale(localeString);
		try {
			BufferedReader file = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(userDir + System.getProperty("file.separator") + "Distiller4.config"), "UTF-8"));
			while (file.ready()) {
				line = file.readLine().trim();
				parms = line.split("=");
				if (parms[0].equals("CONFIG_DIR"))
					com.distiller.config.Messages.setConfigDir(parms[1]);
				else if (parms[0].equals("INPUT_DIR"))
					com.distiller.config.Messages.setInputDir(parms[1]);
				else if (parms[0].equals("OUTPUT_DIR"))
					com.distiller.config.Messages.setOutputDir(parms[1]);
				else if (parms[0].equals("LOCALE"))
					com.distiller.config.Messages.setLocale(parms[1]);
			}
			file.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (com.distiller.config.Messages.getConfigDir().length() == 0)
				com.distiller.config.Messages.setConfigDir(userDir);
			if (com.distiller.config.Messages.getInputDir().length() == 0)
				com.distiller.config.Messages.setInputDir(userDir);
			if (com.distiller.config.Messages.getOutputDir().length() == 0)
				com.distiller.config.Messages.setOutputDir(userDir);
			if (com.distiller.config.Messages.getLocale().length() == 0)
				com.distiller.config.Messages.setLocale("es");
		}
	}

	/**
	 * 
	 * @return HashMap containing the identifiers of the different sets (types) of documents. Returns null if the file cannot be found/read.
	 * Each key-value pair stands for the name you assigned to the document collection and the unique 
	 * title that appears in each document of this collection. 
	 * Example: authors-WIKIPEDIA<p>
	 * Reads "identifier.config" (UTF-8)
	 */
	protected static HashMap<String, String> readIdentifer() {
		HashMap<String, String> identifiers = new HashMap<String, String>();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(com.distiller.config.Messages.getConfigDir()
									+System.getProperty("file.separator")+"identifiers.config"),"UTF-8"));
			  while (reader.ready()) {
				  String pattern = reader.readLine();
				  if ((pattern != null) && (pattern.length() > 0) && (pattern.indexOf("=") > 0)) {
					  String[] pts = pattern.split("=");
					  identifiers.put(pts[0].trim(), pts[1].trim());
				  }
			  }
			  reader.close();
		} catch (FileNotFoundException e) {
			// doesn't matter, we create a new file 
		} catch (IOException e) {
			// shouldn't happen
		}
		if (identifiers.size() == 0)
			return null;
		return identifiers;
	}
	
	/**
	 * Sets key information for the document collection.
	 * It contains at least an entry "ANY" for all documents that do not match any of the identifiers of the registry.
	 * <ul>
	 * <li> document registry: HashMap&#60;String:unique identifier of this document collection, Document:the document itself&#62;
	 * <li> identifier registry: HashMap&#60;String:common name of the document collection, String:unique identifier of this document collection&#62;
	 * </ul>
	 */
	protected static void setUpRegistry() {
		documentRegistry = new HashMap<String, Vector<Document>>();
		identifierRegistry = readIdentifer();
		if ((identifierRegistry != null) && (identifierRegistry.size() > 0)) {
			Set<String> keys = identifierRegistry.keySet();
			for (String key : keys) {
				documentRegistry.put(key, new Vector<Document>());			
			}		
		}
		// add a default entry "ANY"
		documentRegistry.put("ANY", new Vector<Document>());
	}
	
	/**
	 * Fills the document registry. It takes each unique identifier as registered in the identifier register as 
	 * Regular Expression and tries to identify it in the first section of the text (the first 10% of the text's 
	 * characters. It matches with Regex parameters UNICODE_CASE and CASE_INSENSITIVE.
	 * 
	 * @param fileName	String file name of the document (required by Document)
	 * @param text		String content of the document
	 * @return true if the document could be assigned to a set of documents, i.e. it contained a unique title.
	 */
	protected static boolean dispatchText(String fileName, String text) {
		boolean handledAnyText = false;

		while (text.indexOf("  ") >= 0)
			text = text.replaceAll("  ", " ");
		
		Set<String> keys = identifierRegistry.keySet();
		int index = text.length();
		String header = "";
		for (String key : keys) {
			String headerPattern = identifierRegistry.get(key);
			if (headerPattern.length() > 0) {
				java.util.regex.Pattern iPattern = 
						java.util.regex.Pattern.compile(headerPattern, 
								java.util.regex.Pattern.UNICODE_CASE | java.util.regex.Pattern.CASE_INSENSITIVE);
				Matcher iMatcher = iPattern.matcher(text);
				int latestPossiblePosition = (int)(text.length()*.1); // we avoid matchings late in the text that 
				// represent obviously no headers.
				if (iMatcher.find() && (iMatcher.start() < index) && (iMatcher.start() < latestPossiblePosition)) {
					header = key;
					index = iMatcher.start();
				}
			}
		}
		header = header.trim();
		if (header.length() == 0)
			header = "ANY"; 
		/*
		 * The generic sections of the patterns is applied to documents that do not match any of the identifiers stored in identifiers.config.
		 */
		if (header.length() > 0) {
			if (documentRegistry.get(header) != null) {
				handledAnyText = true;
				documentRegistry.get(header).addElement(new Document(fileName, text, header));
			} else {
				System.err.println("No such doc type: "+header);
			}
		}
		return handledAnyText;
	}

	/**
	 * Loads all files from the input path that end with one of the specified file extensions.
	 * fileCollection contains the loaded files.
	 * @return number of files loaded
	 */
	protected static int loadTextFiles() {
		String[] fileExtensions = new String[extensions.size()*2];
		int count = 0;
		for (String extension : extensions) {
			fileExtensions[count++] = extension;
			fileExtensions[count++] = extension.toLowerCase();
		}
		int filesHandled = 0;
		Collection<File> fileCollection = FileUtils.listFiles(
				new File(com.distiller.config.Messages.getInputDir()), fileExtensions, true);
		Iterator<File> files = fileCollection.iterator();
		while (files.hasNext()) {
			String text = "";
			File nextFile = (File)files.next();
			text = readText(nextFile.getPath());
			if ((text != null) && (text.length() > 0)) {
				if (dispatchText(nextFile.getPath(), text))
					filesHandled++;
			}
		} // while
		return filesHandled;
	}
	
	/**
	 * Loads auxiliary files such as month names etc. <p>
	 * It loads the following files:<p>
	 * <ul>
	 * <li> names of months
	 * <li> names of weekdays
	 * <li> numbers as words
	 * <li> file extensions of the documents to be handled.
	 * </ul>
	 * The file names must be specified in messages.properties.<p>
	 * @return true if all files could be loaded.
	 */
	protected static boolean loadSupportFiles() {
		String fileName = com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				com.distiller.config.Messages.getString("file.numbers");
				
		wordlist_numbers = Distiller.readLines(fileName);
		if (wordlist_numbers == null) {
			System.err.println("invalid support file: numbers: "+ fileName);
			return false;
		}
		
		fileName = com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				com.distiller.config.Messages.getString("file.months");
				
		wordlist_months = Distiller.readLines(fileName);
		if (wordlist_months == null) {
			System.err.println("invalid support file: months: "+ fileName);
			return false;
		}

		fileName = com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				com.distiller.config.Messages.getString("file.days");
				
		wordlist_days = Distiller.readLines(fileName);
		if (wordlist_days == null) {
			System.err.println("invalid support file: days: "+ fileName);
			return false;
		}
		
		fileName = com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				com.distiller.config.Messages.getString("file.extensions");
		extensions = Distiller.readLines(fileName);
		if (extensions == null) {
			System.err.println("invalid support file: extensions: "+ fileName);
			return false;
		}
		return true;
	}
	
	/**
	 * Loads all patterns. <p>
	 * It reads the following patterns:<p>
	 * <ul>
	 * <li> connector patterns (required)
	 * <li> basic named entities (required)
	 * <li> named entities (required)
	 * <li> word patterns (required)
	 * <li> location dependent patterns (optional)
	 * @return 0 if all required patterns could be read correctly, otherwise -1.
	 */
	protected static int loadPatterns() {
		System.out.print("[Distiller] load: connector.patterns: ");
		connectorPatternStrings = readLines(com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				"connector.patterns");
		if ((connectorPatternStrings == null) || (connectorPatternStrings.size() == 0)) {
			return -1;
		}
		System.out.println(connectorPatternStrings.size()+" connector patterns");
		
		System.out.print("[Distiller] load: word.patterns: ");
		wordPatternStrings = readLines(com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				"word.patterns");
		if ((wordPatternStrings == null) || (wordPatternStrings.size() == 0)) {
			return -1;
		}
		System.out.println(wordPatternStrings.size()+" word patterns");
		
		System.out.print("[Distiller] load: basic named.entities: ");
		basicNamedEntitiesStrings = readLines(com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				"basic named.entities");
		if ((basicNamedEntitiesStrings == null) || (basicNamedEntitiesStrings.size() == 0)) {
			return -1;
		}
		System.out.println(basicNamedEntitiesStrings.size()+" basic named entities");
		
		System.out.print("[Distiller] load: named.entities: ");
		namedEntitiesStrings = readLines(com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				"named.entities");
		if ((namedEntitiesStrings == null) || (namedEntitiesStrings.size() == 0)) {
			return -1;
		}
		System.out.println(namedEntitiesStrings.size()+" named entities");
		
		System.out.print("[Distiller] load: location-based.patterns: ");
		locationBasedVariablesStrings = readLines(com.distiller.config.Messages.getConfigDir()+
				System.getProperty("file.separator")+
				"location-based.patterns");
		if ((locationBasedVariablesStrings == null) || (locationBasedVariablesStrings.size() == 0)) {
			System.out.println("0 location-based variables");
		}
		else 
			System.out.println(locationBasedVariablesStrings.size()+" location-based variables");
		return 0;
	}
		
	/**
	 * Reads data from a file (UTF-8) and removes all CR and LF.
	 * @param fileName 	String the name of the file (document) to be read
	 * @return Vector&#60;String&#62; of the lines of the file (document). If the file could not be read it returns null.
	 */
	protected static Vector<String>readLines(String fileName) {
		Vector<String> lines = new Vector<String>();
		try {
			BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			while (file.ready()) {
				lines.addElement(file.readLine().replaceAll("[\r\n]", "").trim());
			}
			file.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		if (lines.size() == 0)
			return null;
		return lines;
		
	}
	
	/**
	 * Reads data from a file (UTF-8) and removes all CR and LF.
	 * @param pathName	String the name of the file (document) to be read
	 * @return String: 	the content of the document. If the file could not be read it returns null.
	 */
	protected static String readText(String pathName) {
		String text = "";
		try {
			BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(pathName), "UTF-8"));
			while (file.ready()) {
				text += file.readLine().replaceAll("[\r\n]", "").trim()+System.getProperty("line.separator");
			}
			file.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return text;
		
	}

	/**
	 * Returns the pattern set (four/five pattern groups) for a particular document collection.<p>
	 * Each list of patterns has a general part (usually the first section of the file) and a document specific part.
	 * The specific part starts with &#62;&#62;&#62; followed by the common name of the collection.
	 * @param docType			String common name of the document collection.
	 * @param patternStrings	Vector&#60;String&#62; list of the patterns 
	 * @return					PatternSet: Set of patterns for this specific document collection. Returns null if the input pattern set does not contain data.
	 * @see PatternSet
	 */
	protected static PatternSet getPatternSet(String docType,
			Vector<String> patternStrings) {
		if ((patternStrings == null) || (patternStrings.size() == 0))
				return null;
		Vector<String> localList = new Vector<String>();
		boolean copyOn = true;
		for (String patternString : patternStrings) {
			if (patternString.trim().length() > 0) {
				patternString = patternString.trim();
				if (patternString.length() > 0) {
					if (patternString.startsWith(">>>")) {
						if (patternString.equals(">>>"+docType)) 
							copyOn = true;
						else
							copyOn = false;
					} else {
						if (copyOn)
							localList.addElement(patternString);
					}
				}
			}
		}
		return new PatternSet(localList);
	}
	
	/**
	 * Sets the environment for Distiller. It reads configuration files, auxiliary files, documents, and patterns. If any severe error occurs it stops Distiller.
	 * @param inputDir		String input path (location of the documents to be processed, (optional, can be null, usually Distiller4.config has this information).
	 * @param outputDir		String output path (location for the annotated documents, optional, can be null, usually Distiller4.config has this information).
	 * @param configDir		String directory containing Distiller's configuration data, auxiliary files, and patterns (optional, can be null, usually 
	 * Distiller4.config has this information).
	 * @param localeString	String locale (optional, can be null, usually Distiller4.config has this information)
	 */
	public static void init(String inputDir, String outputDir, String configDir, String localeString) {
		userDir = System.getProperty("user.dir");
		if ((configDir == null) || (configDir.length() == 0))
			configDir = userDir;
		setConfiguration(inputDir, outputDir, configDir, localeString);
		setUpRegistry();
		if ((documentRegistry == null) || (documentRegistry.size() == 0)) {
			System.err.println("document registry invalid");
			System.exit(1);
		}
		
		if ((identifierRegistry == null) || (identifierRegistry.size() == 0)) {
			System.err.println("identifier registry invalid");
			System.exit(1);
		}
		
		if (!loadSupportFiles()) {
			System.err.println("missing essential support files: QUIT");
			System.exit(1);
		}
		
		if (loadPatterns() < 0) {
			System.err.println("missing: QUIT");
			System.exit(1);
		}
		
		int loadedFilesCount = loadTextFiles();
		if (loadedFilesCount == 0) {
			System.err.println("text base empty");
			System.exit(1);
		} else {
			System.out.println("[Distiller] loaded text files: "+loadedFilesCount);
		}		
	}
	
	/**
	 * The annotation process handles all files registered in the document registry. 
	 * It runs correctly when all environment parameters are set. Each annotation of a document runs in a separate thread.
	 * @param statusData Status - holds the information on the process state.
	 * @see Status
	 * @return Vector&#60;Status&#62; information on process results for all documents 
	 */
	public static Vector<Status> annotateFiles(Vector<Status> statusData) {
		Set<String> keys = documentRegistry.keySet();
		for (String key : keys) {
			System.out.println("[Distiller] process "+key);
			PatternSet connectorPatterns = getPatternSet(key, connectorPatternStrings);
			PatternSet wordPatterns = getPatternSet(key, wordPatternStrings);
			PatternSet basicNamedEntities = getPatternSet(key, basicNamedEntitiesStrings);
			PatternSet namedEntities = getPatternSet(key, namedEntitiesStrings);
			PatternSet locationBasedVariables = getPatternSet(key, locationBasedVariablesStrings);
			Vector<Document> documents = documentRegistry.get(key);
			if (documents.size() > 0) {
		        for (int i = 0; i < documents.size(); i++) {
					Status actualStatus = null;
					for (Status status : statusData) {
						if (status.getFileName().equals(documents.elementAt(i).getFileName())) {
							actualStatus = status;
							break;
						}
					}
					if ((actualStatus != null) && (!actualStatus.isInProgress())) {
						actualStatus.setInProgress(true);
					}

		        	AnnotationManager worker = new AnnotationManager(documents.elementAt(i),
		        			wordlist_numbers, wordlist_months, wordlist_days,
		        			connectorPatterns, wordPatterns, basicNamedEntities, namedEntities, locationBasedVariables);
		        	worker.start(actualStatus);
		        }
			}
		}
		return statusData;
	}

	/**
	 * Runs Distiller. It sets up Distiller's environment and starts the annotation process. The annotation of each document runs in a separate thread.
	 * @param args input path, output path, configuration path, locale 
	 */
	public static void main(String[] args) {
		Vector<Status> statusData = new Vector<Status>();
		if (args.length > 3)
			init(args[0], args[1], args[2], args[3]);
		else
			init(null, null, null, null);
		Set<String> keys = documentRegistry.keySet();
		for (String key : keys) {
			Vector<Document> documents = documentRegistry.get(key);
			if (documents.size() > 0) {
		        for (int i = 0; i < documents.size(); i++) {
		        	Status status = new Status();
		        	status.setFileName(documents.elementAt(i).getFileName());
		        	status.setName(documents.elementAt(i).getFileName().substring(
		        			documents.elementAt(i).getFileName().lastIndexOf(System.getProperty("file.separator")) + 1, 
		        			documents.elementAt(i).getFileName().lastIndexOf(".")));
		        	status.setAnnotated(false);
		        	status.setCorrect(false);
		        	status.setInProgress(false);
		        	statusData.addElement(status);
		        }
			}
		}
		
		annotateFiles(statusData);
		System.out.println("[Distiller] bye!");	
		System.exit(0);
	}

}
