package com.distiller.web;

import java.io.Serializable;

/**
 * Status stores the actual process states of a document. Each document has status information assigned to it.
 * The assignment is established through the document's file name (without extension and path information).<p> 
 * Status information must be initialized and assigned before the annotation process is started.
 * Each document can have four states:<p>
 * <ul>
 * <li>in progress
 * <li>filtered (not used anymore)
 * <li>annotated
 * <li>correct
 * </ul>
 * 
 * @author Kurt Englmeier
 *
 */
public class Status implements Serializable {

	private static final long serialVersionUID = 1768758513064575409L;
	private String name = ""; // name of the document without path information and extension
	private String fileName = ""; // full file name including path information and extension
	private boolean filtered = false;
	private boolean annotated = false;
	private boolean correct = false;
	private boolean inProgress = false;
	
	/**
	 * @return true if the document is being processed.
	 */
	public boolean isInProgress() {
		return inProgress;
	}
	/**
	 * @param inProgress - sets the status "in progress".
	 */
	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}
	/**
	 * @return the name of the document (without extension and path information)
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name String: the name of the document. The name must be without extension and path information.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the complete file name of the document
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName - String: sets the complete file name of the document
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return filtered - indicates if the document is filtered or not - not used anymore
	 */
	public boolean isFiltered() {
		return filtered;
	}
	/**
	 * @param filtered sets the state "filtered" that indicates if the document is filtered or not - not used anymore
	 */
	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}
	/**
	 * @return - indicates if the document is annotated (true) or not (false)
	 */
	public boolean isAnnotated() {
		return annotated;
	}
	/**
	 * @param annotated sets the state "annotated" that indicates if the document is annotated or not
	 */
	public void setAnnotated(boolean annotated) {
		this.annotated = annotated;
	}
	/**
	 * @return - indicates if the document is correct (true) or not (false)
	 */
	public boolean isCorrect() {
		return correct;
	}
	/**
	 * @param correct sets the state "correct" that indicates if the document is correct or not
	 */
	public void setCorrect(boolean correct) {
		this.correct = correct;
	}
}

