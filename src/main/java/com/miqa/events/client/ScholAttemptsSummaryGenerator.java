package com.miqa.events.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ScholAttemptsSummaryGenerator implements Job {

	static Connection conn = null;
	int totalCounter = 0;

	public static void main(String[] args) throws SQLException, ParseException {
		AttemptsSummaryGenerator gen = new AttemptsSummaryGenerator();
		gen.generateSummary();
		// gen.getAnswerChoiceToOptionLabel();
	}

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			generateSummary();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean generateSummary() throws SQLException, ParseException {
		System.out
				.println("****************BATCH BEGINS ************************"
						+ new Date());
		/*
		 * String startTime = readStartTime(); Date startD = getDate(startTime);
		 */
		try {
			Date currTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
					.getTime();
			int testRun = 0;
			long startId = getStartId();
			long maxId = getMaxId();
			long endId = 0;
			int attemptCount = 0;
			Map<Integer, Integer> ansChoiceToOptionLabel = null;
			if (startId < maxId)
				ansChoiceToOptionLabel = getAnswerChoiceToOptionLabel();

			while (startId > 0 && startId < maxId) {
				// if(testRun > 10) break;
				// testRun++;
				endId = startId + 25000 <= maxId ? startId + 25000 : maxId;
				// System.out.println("before getting attempts " +startId +
				// " - "+ endId);
				List<Attempt> attempts = getRecords(startId, endId);
				// System.out.println(" attempts size = "+attempts.size());
				attemptCount = attemptCount + attempts.size();

				createSummaryVO(attempts, ansChoiceToOptionLabel, currTime);
				// System.out.println("After creating summary objects " );

				startId = endId + 1;
				updateStartId(startId);
				getConnection().commit();

			}
			// System.out.println(" totalCounter = "+totalCounter);
			// System.out.println(" attemptCount = "+attemptCount);
			// System.out.println("done");
			// String dateStr = dateToString(startD);

		} catch (Exception e) {
			e.printStackTrace();
			if (conn != null)
				getConnection().rollback();
			closeConnection();

			return false;
		}
		closeConnection();
		System.out
				.println("****************BATCH COMPLETED ************************"
						+ new Date());
		return true;

	}

	private void updateStartId(long endId) throws SQLException {

		Connection conn = getConnection();
		String sql = null;
		sql = "update es_setting set value = ? where name = 'attempt_summary_curr_id'";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, String.valueOf(endId));

		ps.executeUpdate();
		ps.close();
	}

	

	private long getMaxId() throws SQLException {
		String sql = null;
		sql = "select max(id) as max_id from es_quiz_session";
		
		Connection conn = getConnection();
		java.sql.PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		rs.next();
		String retVal = rs.getString("max_id");
		rs.close();
		ps.close();
		// conn.close();
		return Long.parseLong(retVal);
	}

	private long getStartId() throws SQLException {
		String sql = null;
		sql = "select value from es_setting where name = 'attempt_summary_curr_id' ";
		
		Connection conn = getConnection();
		java.sql.PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		rs.next();

		String retVal = rs.getString("value");
		rs.close();
		ps.close();
		// conn.close();
		return Long.parseLong(retVal);
	}

	private void closeConnection() throws SQLException {
		if (conn != null)
			conn.close();
		conn = null;
		return;
	}

	private void updateStartTime(String dateStr) throws SQLException {
		Connection conn = getConnection();
		String	sql = "update es_setting set attempt_summary_start_time = ?";

		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, dateStr);

		ps.executeUpdate();
		ps.close();
		// conn.close();

	}

	private String dateToString(Date d) {
		String format = "yyyy-MM-dd'T'HH:mm:ss";
		DateFormat df = new SimpleDateFormat(format);

		String retVal = df.format(d);
		return retVal;
	}

	private Date getDate(String startTime) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		// startTime = "2014-10-05T15:23:01";
		Date date = formatter.parse(startTime);
		return date;

	}

	private Map<Integer, Integer> getAnswerChoiceToOptionLabel()
			throws SQLException {
		String sql = null;
		sql = "SELECT id, question_id, @choice:=CASE WHEN @question <> question_id "
				+ "THEN 0 ELSE @choice+1 END AS rn, @question:=question_id AS clset FROM "
				+ "(SELECT @choice:= -1) s, (SELECT @question:= -1) c, "
				+ "(SELECT * FROM es_answer_choice ORDER BY question_id, id) t";

		
		Connection conn = getConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		Map<Integer, Integer> retVal = new HashMap<Integer, Integer>();
		while (rs.next()) {
			int choiceId = rs.getInt("id");
			int rank = rs.getInt("rn");
			int questionId = rs.getInt("question_id");
			// System.out.println(" choiceId = "+choiceId + " rank = "+rank +
			// " questionId = "+questionId);
			retVal.put(choiceId, rank);
		}
		rs.close();
		ps.close();
		System.out.println(" getAnswerChoiceToOptionLabel " + retVal.size());
		return retVal;
	}

	private String readStartTime() throws SQLException {
		String sql = null;
		sql = "select value from es_setting where name = 'attempt_summary_start_time' ";

		Connection conn = getConnection();
		java.sql.PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		rs.next();
		String retVal = rs.getString("value");
		rs.close();
		ps.close();
		// conn.close();
		return retVal;

	}

	private static Connection getConnection() throws SQLException {

		if (conn != null && !conn.isClosed())
			return conn;

			System.out.println("creating and  returning a new connection");

			String serverName = "10.210.0.74";
			String portNumber = "3306";
			String dbms = "mysql";
			String userName = "escholarpriv";
			String password = "EgmatEScholDB@dm!n";

			
			serverName = "localhost";
			userName = "root";
			password = "";

			Properties connectionProps = new Properties();
			connectionProps.put("user", userName);
			connectionProps.put("password", password);

			conn = DriverManager.getConnection("jdbc:" + dbms + "://"
					+ serverName + ":" + portNumber + "/escholar6",
					connectionProps);

			System.out.println("Connected to database");
			conn.setAutoCommit(false);
			return conn;
		
	}

	private void createSummaryVO(List<Attempt> attempts,
			Map<Integer, Integer> ansChoiceToOptionLabel, Date currTime)
			throws SQLException {

		List<AttemptSummary> oldList = getOldAttemptSummary();
		Map<String, AttemptSummary> keyToAttemptSummary = createAttemptSummaryMap(oldList);
		// Map<String,List<Attemp>> keyToAttempts =
		// createKeyToAttempts(attemps);

		for (Attempt attempt : attempts) {
			int questionId = attempt.getQuestionId();
			int majorVersion = attempt.getMajorVersion();
			// String key = "" + questionId + "_" + majorVersion + "_"
			// + attempt.isFirstAttempt();
			String key = createKey(questionId, majorVersion,
					attempt.isFirstAttempt());
			AttemptSummary oldSummary = keyToAttemptSummary.get(key);
			if (oldSummary == null) {
				oldSummary = updateOldSummary(oldSummary, attempt,
						ansChoiceToOptionLabel, currTime);
				/*
				 * String oldSummKey = oldSummary.getQuestionId() + "_" +
				 * oldSummary.getMajorVersion() + "_" +
				 * oldSummary.isFirstAttempt();
				 */
				String oldSummKey = createKey(oldSummary.getQuestionId(),
						oldSummary.getMajorVersion(),
						oldSummary.isFirstAttempt());
				keyToAttemptSummary.put(oldSummKey, oldSummary);
			} else {
				oldSummary = updateOldSummary(oldSummary, attempt,
						ansChoiceToOptionLabel, currTime);

			}
		}
		updateDB(keyToAttemptSummary, currTime);

	}

	private List<AttemptSummary> getOldAttemptSummary() throws SQLException {
		/*
		 * private long id; private int questionId; private String questionName;
		 * private int correctCount; private int inCorrectCount; private int
		 * majorVersion; private int minorVersion;
		 * 
		 * private String questionBankName; private String quizTemplateName;
		 * private int choiceACount; private int choiceBCount; private int
		 * choiceCCount; private int choiceDCount; private int choiceECount;
		 * 
		 * private Date timestamp; private boolean isFirstAttempt; private
		 * String DifficultyLevel; private int totalCount;
		 */
		Connection conn = getConnection();
		String sql = null;
			sql = "select * from es_attempt_summary ";

		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<AttemptSummary> retVal = new ArrayList<AttemptSummary>();

		while (rs.next()) {
			int id = rs.getInt("id");
			int correctCount = rs.getInt("correct_count");
			int incorrectCount = rs.getInt("incorrect_count");
			int choiceACount = rs.getInt("choice_a_count");
			int choiceBCount = rs.getInt("choice_b_count");
			int choiceCCount = rs.getInt("choice_c_count");
			int choiceDCount = rs.getInt("choice_d_count");
			int choiceECount = rs.getInt("choice_e_count");

			int majorVersion = rs.getInt("major_version");
			int minorVersion = rs.getInt("minor_version");
			String qbName = "empty";
			String qtName = "empty";
			
			int questionId = rs.getInt("question_id");
			String questionName = rs.getString("question_name");

			
			Date timestamp = rs.getDate("last_modified_date");
			boolean firstAttempt = rs.getBoolean("is_first_attempt");
			String difficultyLevel = rs.getString("difficulty_level");
			int totalCount = rs.getInt("total_count");

			AttemptSummary summ = new AttemptSummary();
			summ.setId(id);
			summ.setQuestionId(questionId);
			summ.setQuestionName(questionName);
			summ.setCorrectCount(correctCount);
			summ.setInCorrectCount(incorrectCount);
			summ.setChoiceACount(choiceACount);
			summ.setChoiceBCount(choiceBCount);
			summ.setChoiceCCount(choiceCCount);
			summ.setChoiceDCount(choiceDCount);
			summ.setChoiceECount(choiceECount);
			summ.setMajorVersion(majorVersion);
			summ.setMinorVersion(minorVersion);
			summ.setQuestionBankName(qbName);
			summ.setQuizTemplateName(qtName);
			summ.setTimestamp(timestamp);
			summ.setFirstAttempt(firstAttempt);
			summ.setDifficultyLevel(difficultyLevel);
			summ.setTotalCount(totalCount);

			retVal.add(summ);

		}
		rs.close();
		ps.close();
		return retVal;

	}

	private Map<String, AttemptSummary> createAttemptSummaryMap(
			List<AttemptSummary> oldList) {
		Map<String, AttemptSummary> retVal = new HashMap<String, AttemptSummary>();
		for (AttemptSummary summ : oldList) {
			int qId = summ.getQuestionId();
			int majorVersion = summ.getMajorVersion();
			boolean firstAttempt = summ.isFirstAttempt();
			String key = createKey(qId, majorVersion, firstAttempt);
			retVal.put(key, summ);
		}
		return retVal;
	}

	private String createKey(int qId, int majorVersion, boolean firstAttempt) {

		StringBuilder key = new StringBuilder().append(String.valueOf(qId))
				.append("_").append(String.valueOf(majorVersion)).append("_")
				.append(String.valueOf(firstAttempt));
		return key.toString();
	}

	private AttemptSummary updateOldSummary(AttemptSummary oldSummary,
			Attempt attempt, Map<Integer, Integer> ansChoiceToOptionLabel,
			Date timestamp) {

		// private long id;
		// private int questionId;
		// private String questionName;
		// private int correctCount;
		// private int inCorrectCount;
		// private int majorVersion;
		// private int minorVersion;
		//
		// private String questionBankName;
		// private String quizTemplateName;
		// private int choiceACount;
		// private int choiceBCount;
		// private int choiceCCount;
		// private int choiceDCount;
		// private int choiceECount;
		//
		// private Date timestamp;
		// private boolean isFirstAttempt;
		// private String DifficultyLevel;
		// private int totalCount;

		if (oldSummary == null) {
			// System.out.println("creating new summary object");
			oldSummary = new AttemptSummary();
			// oldSummary.setCorrectCount(attempt.isCorrect() ? 1 : 0);
			// oldSummary.setInCorrectCount(attempt.isCorrect() ? 0 : 1);
			// oldSummary.setFirstAttempt(attempt.isFirstAttempt());

		}

		oldSummary.setQuestionId(attempt.getQuestionId());
		oldSummary.setQuestionName(attempt.getQuestionName());
		oldSummary.setCorrectCount(oldSummary.getCorrectCount()
				+ (attempt.isCorrect() ? 1 : 0));
		oldSummary.setInCorrectCount(oldSummary.getInCorrectCount()
				+ (attempt.isCorrect() ? 0 : 1));
		oldSummary.setFirstAttempt(attempt.isFirstAttempt());

		if (attempt.isCorrect()) {
			oldSummary.setCorrectDuration(oldSummary.getCorrectDuration()
					+ attempt.getDuration());
		} else {
			oldSummary.setIncorrectDuration(oldSummary.getIncorrectDuration()
					+ attempt.getDuration());
		}
		oldSummary.setTotalDuration(oldSummary.getTotalDuration()
				+ attempt.getDuration());
		oldSummary.setMajorVersion(attempt.getMajorVersion());
		oldSummary.setMinorVersion(attempt.getMinorVersion());
		oldSummary.setQuestionBankName(attempt.getQuestionBankName());
		oldSummary.setQuizTemplateName(attempt.getQuizTemplatteName());
		updateChoiceCount(oldSummary, ansChoiceToOptionLabel, attempt);
		oldSummary.setTimestamp(timestamp);
		oldSummary.setDifficultyLevel(attempt.getDifficultyLevel());
		oldSummary.setTotalCount(oldSummary.getTotalCount() + 1);
		totalCounter = totalCounter + 1;
		// oldSummary.setQuestionEnabled(attempt.isQuestionEnabled());

		return oldSummary;
	}

	private void updateChoiceCount(AttemptSummary oldSummary,
			Map<Integer, Integer> ansChoiceToOptionLabel, Attempt attempt) {

		int choiceId = attempt.getChoiceId();
		// System.out.println("got choice id as "+choiceId);
		int option = ansChoiceToOptionLabel.get(choiceId);
		// System.out.println("got option as "+option);
		/*
		 * if(option >= 5) {
		 * System.out.println("Error option greater than E  "); System.exit(1);
		 * }
		 */
		switch (option) {

		case 0:
			oldSummary.setChoiceACount(oldSummary.getChoiceACount() + 1);
			break;
		case 1:
			oldSummary.setChoiceBCount(oldSummary.getChoiceBCount() + 1);
			break;
		case 2:
			oldSummary.setChoiceCCount(oldSummary.getChoiceCCount() + 1);
			break;
		case 3:
			oldSummary.setChoiceDCount(oldSummary.getChoiceDCount() + 1);
			break;
		case 4:
			oldSummary.setChoiceECount(oldSummary.getChoiceECount() + 1);
			break;
		}

	}

	private void updateDB(Map<String, AttemptSummary> keyToAttemptSummary,
			Date currTime) throws SQLException {
		Iterator<String> iter = keyToAttemptSummary.keySet().iterator();
		Connection conn = getConnection();
		String updateSQL = null;
		updateSQL = "update es_attempt_summary set question_id = ? , question_name=?,correct_count = ? , incorrect_count = ?, "
					+ "major_version = ? , minor_version = ?  ,choice_a_count = ? , "
					+ "choice_b_count = ? , choice_c_count = ? ,choice_d_count = ? , choice_e_count =  ? ,total_count=? ,  last_modified_date  = ? ,"
					+ "is_first_attempt=? ,difficulty_level = ?,incorrect_duration =?  ,correct_duration = ?  , total_duration = ? where id = ?   ";
		
		String insertSQL = null;
		insertSQL = "insert into es_attempt_summary  (question_id,question_name,correct_count,incorrect_count,major_version,minor_version"
					+ ",choice_a_count,choice_b_count,choice_c_count,choice_d_count,choice_e_count,total_count,last_modified_date,"
					+ "is_first_attempt,difficulty_level, incorrect_duration  ,correct_duration  , total_duration) 	"
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
		
		int batchSize = 2000;
		int count = 0;
		PreparedStatement ps = null;
		while (iter.hasNext()) {
			String key = iter.next();
			AttemptSummary summary = keyToAttemptSummary.get(key);
			String sql = null;
			if (summary.getId() > 0) {
				sql = updateSQL;
				System.out.println(" executing update  sql ");

			} else {
				sql = insertSQL;
				System.out.println(" executing insert  sql ");

			}
			ps = conn.prepareStatement(sql);

			setPreparedStatement(summary, ps, currTime);
			try {
				// System.out.println("ps = "+ps);
				// ps.executeUpdate();

				ps.addBatch();

			} catch (SQLException e) {
				// System.out.println("ps = "+ps);
				throw e;
			}
			ps.executeBatch();
			ps.clearBatch();
			ps.clearParameters();
			ps.close();

			// ps.addBatch();
			// if (++count % batchSize == 0) {
			// System.out.println(ps);
			// ps.executeBatch();
			// }
		}
		// if(ps != null) ps.executeBatch();
		// if(ps != null) ps.close();
		// if(ps != null) conn.close();
	}

	private void setPreparedStatement(AttemptSummary summary,
			PreparedStatement ps, Date currTime) throws SQLException {

			ps.setInt(1, summary.getQuestionId());
			ps.setString(2, summary.getQuestionName());

			ps.setInt(3, summary.getCorrectCount());
			ps.setInt(4, summary.getInCorrectCount());
			ps.setInt(5, summary.getMajorVersion());
			ps.setInt(6, summary.getMinorVersion());
			ps.setInt(7, summary.getChoiceACount());
			ps.setInt(8, summary.getChoiceBCount());
			ps.setInt(9, summary.getChoiceCCount());
			ps.setInt(10, summary.getChoiceDCount());
			ps.setInt(11, summary.getChoiceECount());
			ps.setInt(12, summary.getTotalCount());
			ps.setTimestamp(13, new Timestamp(currTime.getTime()));
			ps.setBoolean(14, summary.isFirstAttempt());
			ps.setString(15, summary.getDifficultyLevel());
			ps.setLong(16, summary.getIncorrectDuration());
			ps.setLong(17, summary.getCorrectDuration());
			ps.setLong(18, summary.getTotalDuration());
			// ps.setBoolean(18,summary.isQuestionEnabled());
			if (summary.getId() > 0) {
				// System.out.println("	ary id "+ summary.getId());
				ps.setLong(19, summary.getId());
			}

		 
		

	}

	public List<Attempt> getRecords(long startId, long endId) throws Exception {
		/*
		 * private int questionId; private long attemptId; private long userId;
		 * private long quizId; private int choiceId; private boolean isCorrect;
		 * private boolean isFirstAttempt;
		 * 
		 * private String difficultyLevel; private String quizTemplatteName;
		 * private String questionBankName; private int quizTemplateId; private
		 * int questionBankId; private String questionName; private int
		 * majorVersion; private int minorVersion ;
		 */

		System.out.println(" getting records for ids between  " + startId
				+ " and " + endId);

		String sql = null;
		sql = "select qs.* ,q.status as question_status, q.name as question_name from es_quiz_session qs , es_question q where "
					+ "qs.id >= ? and qs.id <= ? and qs.question_id = q.id and qs.answer_chosen > 0";

		
		Connection conn = getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sql);

		// java.sql.Timestamp sqlStartDate = new java.sql.Timestamp(startId);
		// java.sql.Timestamp sqlEndDate = new
		// java.sql.Timestamp(endTime.getTime());
		pstmt.setLong(1, startId);
		pstmt.setLong(2, endId);

		// System.out.println("sqlStart "+sqlStartDate);
		// System.out.println("sqlEnd "+sqlEndDate);
		// System.out.println(pstmt);
		ResultSet rs = pstmt.executeQuery();
		// System.out.println("query executed  ");

		List<Attempt> retVal = new ArrayList<Attempt>();

		while (rs.next()) {
			Attempt attempt = new Attempt();
			attempt.setQuestionId(rs.getInt("question_id"));
			attempt.setQuestionName(rs.getString("question_name"));
			attempt.setAttemptId(rs.getLong("id"));
			attempt.setUserId(rs.getLong("user_id"));
			attempt.setFirstAttempt(rs.getBoolean("is_first_attempt"));
			attempt.setCorrect(rs.getBoolean("is_correct"));
			attempt.setQuizId(rs.getLong("quiz_id"));
			attempt.setChoiceId(rs.getInt("answer_chosen"));
			attempt.setDifficultyLevel(rs.getString("difficulty_level"));
			
			attempt.setMajorVersion(rs.getInt("major_version_of_question"));
			attempt.setMinorVersion(rs.getInt("minor_version_of_question"));
			int duration = rs.getInt("duration");
			attempt.setDuration(duration);

			// String status = rs.getString("question_status");
			// System.out.println("got status as "+status);
			// boolean questionEnabled = status.equals("ENABLED") ? true:false;
			// attempt.setQuestionEnabled(questionEnabled);

			if (attempt.getQuestionId() == 0)
				throw new Exception("Invalid question id "
						+ attempt.getQuestionId());
			retVal.add(attempt);

		}
		rs.close();
		pstmt.close();
		return retVal;
	}
}
