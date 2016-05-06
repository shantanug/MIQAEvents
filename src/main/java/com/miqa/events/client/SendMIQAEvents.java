package com.miqa.events.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendMIQAEvents implements Job {
	private static final String hostname = "10.211.65.126";
	private static final String user = "miqaprivate";
	private static final String password = "EgmatMIQADB@dmin";
	private static final String db = "miqa_prod_7thOct";
	private static final String segmentAPIKey = "5vHttpjYtGZ0fCntVX6x7cwwZ2DHGhQj";
	private static final String kmAPIKey = "99533a732f54096d7a0d1e0ad72ad923292f1933";
	private static final int DIAGNOSTIC_QUIZ = 499;
	private static final int QUALIFYING_QUIZ = 520;
	private static final String MARKER_FILE =  "marker.txt" ;	
	
	

	public static void main(String [] args) {
		new SendMIQAEvents().execute();
	}
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		execute();

	}
	public  void execute(){
		System.out.println("executing cron to push miqa events"+new Date());
		Connection conn = null;
		PreparedStatement stmt = null;
		int lastQuizMarker = (int)getLastQuizMarker();
		int maxQuizID = -1;
		if (lastQuizMarker < 0) {
			System.out.println("could not find last quiz marker - returning ");
			return;
		}
		System.out.println("got last quizmarker as "+lastQuizMarker);
		ResultSet rs = null;
		try {
			// new com.mysql.jdbc.Driver();
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			// conn =
			// DriverManager.getConnection("jdbc:mysql://localhost:3306/testdatabase?user=testuser&password=testpassword");
			String connectionUrl = "jdbc:mysql://" + hostname + ":3306/" + db;
			String connectionUser = user;
			String connectionPassword = password;
			String sql = "SELECT u.username as userName ,q.id as quizId,q.quiz_template_id as quizTemplateId ,q.quiz_end_mode  as quizEndMode,q.creation_date as quizCreationTime ,q.time_taken as quizTimeTaken, q.user_score as userScore, q.max_score as maxScore,q.creation_date,q.actual_time_taken FROM miqa_quiz q, miqa_user u where q.quiz_end_mode in ('TIMEOUT','FULLYCOMPLETE') and quiz_template_id in (499,520) and  q.user_id = u.id and q.id > "+lastQuizMarker+"  order by q.id";
			conn = DriverManager.getConnection(connectionUrl, connectionUser,
					connectionPassword);
			stmt = conn.prepareStatement(sql);
			
			//stmt.setInt(1, lastQuizMarker);
//			stmt.setInt(2, 499);
//			stmt.setInt(3, 520);

			rs = stmt.executeQuery(sql);
			long quizId = -1;
			while (rs.next()) {
				String userName = rs.getString("userName");
				int templateId = rs.getInt("quizTemplateId");
				String quizEndMode = rs.getString("quizEndMode");
				double userScore = rs.getDouble("userScore");
				double maxScore = rs.getDouble("maxScore");
				int quizTimeTaken = rs.getInt("quizTimeTaken");
				long epochT = (rs.getDate("quizCreationTime").getTime()/1000);
				Map<String,String> m = new HashMap<String,String>();
				m.put("startTime", ""+epochT);
				quizId = rs.getLong("quizId");
				KissmetricsClient.pushEvent(userName,
						getEventName("start", templateId), m,
						kmAPIKey, segmentAPIKey);
				
				System.out.println("");
				if (quizEndMode != null
						&& (quizEndMode.equals("TIMEOUT") || quizEndMode
								.equals("FULLYCOMPLETE"))) {
					String passFailTag = getPassFailTag(userScore, maxScore, templateId);
//					m = new  HashMap<String,String>();
					m.put("endTime", ""+(epochT+quizTimeTaken));
					System.out.println("endTime = "+m.get("endTime"));
					m.put("Result",passFailTag);
					m.put("Percentage",getPercentage(userScore, maxScore));
					KissmetricsClient.pushEvent(userName,
							getEventName("end", templateId), m,
							kmAPIKey, segmentAPIKey);
				}

			}
			if (quizId > lastQuizMarker) {
				lastQuizMarker = (int)quizId;
				saveQuizMarker(lastQuizMarker);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveQuizMarker(long lastQuizMarker) {
		System.out.println(" output to marker "+lastQuizMarker);
		Writer wr = null;
		try {
			
			PrintWriter writer = new PrintWriter(MARKER_FILE);
			writer.print("");
			writer.close();

			wr = new FileWriter("marker.txt");
			wr.write(lastQuizMarker + "");
			wr.close();	

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
	private String getPercentage(double userScore, double maxScore) {
		double percentD = Math.round((userScore / maxScore) * 100);
		long percent = (long) Math.round(percentD);
		System.out.println("returning percent as "+percent);
		return ""+percent;
	}

	private String getPassFailTag(double userScore, double maxScore,
			int templateId) {
		double percentD = Math.round((userScore / maxScore) * 100);
		long percent = (long) Math.round(percentD);
		if (templateId == DIAGNOSTIC_QUIZ) {
			if (percent >= 80) {
				return "PASS";
			} else {
				return "FAIL";
			}
		} else if (templateId == QUALIFYING_QUIZ) {
			if (percent < 62) {
				return "FAIL";
			} else if(percent >=62 && percent <=79) {
				return "INTERMEDIATE";
			}
			else if(percent > 79){
				return "PASS";
			}else {
				return "NA";
			}
		}
		System.out.println("ERROR: UNKNOWN for userScore " + userScore
				+ ": maxScore " + maxScore + " : templateID : " + templateId);
		return "UNKNOWN";
	}

	private int getLastQuizMarker() {

		Scanner scanner;
		try {
			scanner = new Scanner(new File(MARKER_FILE));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		int retVal = -1;
		while (scanner.hasNextInt()) {
			retVal = scanner.nextInt();
		}
		return retVal;
	}

	private String getEventName(String startOrEnd, int templateId) {

		if (templateId == DIAGNOSTIC_QUIZ && startOrEnd.equals("start")) {
			return "Start Diagnostic";
		} else if (templateId == DIAGNOSTIC_QUIZ && startOrEnd.equals("end")) {
			return "End Diagnostic";
		} else if (templateId == QUALIFYING_QUIZ && startOrEnd.equals("start")) {
			return "Start Qualifying Quiz";
		} else if (templateId == QUALIFYING_QUIZ && startOrEnd.equals("end")) {
			return "End Qualifying Quiz";
		} else {
			System.out.println("Could not figure out quiz type " + startOrEnd
					+ " : template ID " + templateId);
			return null;
		}
	}

}
