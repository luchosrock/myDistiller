package com.distiller.docs;
/**
 * Document contains a few important data on the document to be processed.<p>
 * Each document is classified by its unique title (defined in identifiers.config). 
 * 
 * @author Kurt Englmeier
 *
 */
public class Document {
	
	private String text = null;
	private String fileName = null;
	private String type = null;
	
	/**
	 * Sets an instance for the document to be processed.
	 * @param fileName 	the complete file name of the original document.
	 * @param text 		the content of the document
	 * @param type 		the unique title as defined in identifiers.config.
	 */
	public Document(String fileName, String text, String type) {
		super();
		this.text = text;
		this.fileName = fileName;
		this.type = type;
	}

	/**
	 * Returns the unique identifier of this document.
	 * @return Returns the identifier of this document
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the unique identifier for this document.
	 * @param type String the identifier of this document
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the content of this document.
	 * @return Returns content of this document
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the content of this document.
	 * @param text String the content of this document
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Returns the file name of this document.
	 * @return Returns the file name of this document
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name of this document.
	 * @param fileName String the file name of this document
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * @return Returns the unique identifier and the file name of this document in one string.
	 */
	public String toString() {
		return type+":"+fileName;
	}

}
