package com.miqa.events.client;

public class Attempt {
	private int questionId;
	private long attemptId;
	private long userId;
	private long quizId;
	private int choiceId;
	private boolean isCorrect;
	private boolean isFirstAttempt;
	
	private String difficultyLevel;
	private String quizTemplatteName;
	private String questionBankName;
	private int quizTemplateId;
	private int  questionBankId;
	private String questionName;
	private int majorVersion;
	private int minorVersion ; 

	public int getQuestionId() {
		return questionId;
	}
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public long getAttemptId() {
		return attemptId;
	}
	public void setAttemptId(long attemptId) {
		this.attemptId = attemptId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getQuizId() {
		return quizId;
	}
	public void setQuizId(long quizId) {
		this.quizId = quizId;
	}
	public int getChoiceId() {
		return choiceId;
	}
	public void setChoiceId(int choiceId) {
		this.choiceId = choiceId;
	}
	public boolean isCorrect() {
		return isCorrect;
	}
	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}
	public boolean isFirstAttempt() {
		return isFirstAttempt;
	}
	public void setFirstAttempt(boolean isFirstAttempt) {
		this.isFirstAttempt = isFirstAttempt;
	}
	public String getDifficultyLevel() {
		return difficultyLevel;
	}
	public void setDifficultyLevel(String difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}
	public String getQuizTemplatteName() {
		return quizTemplatteName;
	}
	public void setQuizTemplatteName(String quizTemplatteName) {
		this.quizTemplatteName = quizTemplatteName;
	}
	public String getQuestionBankName() {
		return questionBankName;
	}
	public void setQuestionBankName(String questionBankName) {
		this.questionBankName = questionBankName;
	}
	public int getQuizTemplateId() {
		return quizTemplateId;
	}
	public void setQuizTemplateId(int quizTemplateId) {
		this.quizTemplateId = quizTemplateId;
	}
	public int getQuestionBankId() {
		return questionBankId;
	}
	public void setQuestionBankId(int questionBankId) {
		this.questionBankId = questionBankId;
	}
	public String getQuestionName() {
		return questionName;
	}
	public void setQuestionName(String questionName) {
		this.questionName = questionName;
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
	
	
	
}