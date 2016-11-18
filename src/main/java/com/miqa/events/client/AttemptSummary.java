package com.miqa.events.client;

import java.util.Date;

public class AttemptSummary {
	private long id;
	private int questionId;
	private String questionName;
	private int correctCount;
	private int inCorrectCount;
	private int majorVersion;
	private int minorVersion;
	
	private String questionBankName;
	private String quizTemplateName;
	private int choiceACount;
	private int choiceBCount;
	private int choiceCCount;
	private int choiceDCount;
	private int choiceECount;
	
	private Date timestamp;
	private boolean isFirstAttempt;
	private String DifficultyLevel;
	private int totalCount;

	public int getQuestionId() {
		return questionId;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public String getQuestionName() {
		return questionName;
	}
	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}
	public int getCorrectCount() {
		return correctCount;
	}
	public void setCorrectCount(int correctCount) {
		this.correctCount = correctCount;
	}
	public int getInCorrectCount() {
		return inCorrectCount;
	}
	public void setInCorrectCount(int inCorrectCount) {
		this.inCorrectCount = inCorrectCount;
	}
	public int getMajorVersion() {
		return majorVersion;
	}
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}
	public int getMinorVersion() {
		return minorVersion;
	}
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}
	public String getQuestionBankName() {
		return questionBankName;
	}
	public void setQuestionBankName(String questionBankName) {
		this.questionBankName = questionBankName;
	}
	public String getQuizTemplateName() {
		return quizTemplateName;
	}
	public void setQuizTemplateName(String quizTemplateName) {
		this.quizTemplateName = quizTemplateName;
	}
	public int getChoiceACount() {
		return choiceACount;
	}
	public void setChoiceACount(int choiceACount) {
		this.choiceACount = choiceACount;
	}
	public int getChoiceBCount() {
		return choiceBCount;
	}
	public void setChoiceBCount(int choiceBCount) {
		this.choiceBCount = choiceBCount;
	}
	public int getChoiceCCount() {
		return choiceCCount;
	}
	public void setChoiceCCount(int choiceCCount) {
		this.choiceCCount = choiceCCount;
	}
	public int getChoiceDCount() {
		return choiceDCount;
	}
	public void setChoiceDCount(int choiceDCount) {
		this.choiceDCount = choiceDCount;
	}
	public int getChoiceECount() {
		return choiceECount;
	}
	public void setChoiceECount(int choiceECount) {
		this.choiceECount = choiceECount;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isFirstAttempt() {
		return isFirstAttempt;
	}
	public void setFirstAttempt(boolean isFirstAttempt) {
		this.isFirstAttempt = isFirstAttempt;
	}
	public String getDifficultyLevel() {
		return DifficultyLevel;
	}
	public void setDifficultyLevel(String difficultyLevel) {
		DifficultyLevel = difficultyLevel;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
	
	
	
}
