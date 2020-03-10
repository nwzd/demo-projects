package com.demo.fact;

public class WordBuilder {

	private String word;
	private int occurence;
	private Significance level;

	public WordBuilder() {
	}

	public WordBuilder word(String word) {
		this.word = word;
		return this;
	}

	public WordBuilder occurence(int occurence) {
		this.occurence = occurence;
		return this;
	}

	public WordBuilder level(Significance level) {
		this.level = level;
		return this;
	}

	public Word build() {
		return new Word(word, occurence, level);
	}
	
}
