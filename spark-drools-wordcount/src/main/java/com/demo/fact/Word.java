package com.demo.fact;

import java.io.Serializable;

public class Word implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8192689485744322836L;
	private String word;
	private int occurence;
	private Significance level;

	public Word(String word, int occurence, Significance level) {
		super();
		this.word = word;
		this.occurence = occurence;
		this.level = level;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getOccurence() {
		return occurence;
	}

	public void setOccurence(int occurence) {
		this.occurence = occurence;
	}

	public Significance getLevel() {
		return level;
	}

	public void setLevel(Significance level) {
		this.level = level;
	}

	
}
