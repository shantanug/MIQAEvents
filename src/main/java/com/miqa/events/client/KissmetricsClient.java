package com.miqa.events.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.TrackMessage;

public class KissmetricsClient {

	private static final String START_DIAGNOSTIC = "Start Diagnostic";
	private static final String END_DIAGNOSTIC =  "End Diagnostic";
	
	private static final String START_QUALIFYING = "Start Qualifying";
	private static final String END_QUALIFYING =  "End Qualifying";
	
	static Analytics analytics = Analytics.builder("5vHttpjYtGZ0fCntVX6x7cwwZ2DHGhQj").build();

	

	public static void main(String[] args) {
//		Map<String, String> utmMap = new HashMap<String, String>();
//		Integer templateID = 4;
//		utmMap.put(Constants.UTM_CHANNEL, "gcbanners");
//		utmMap.put(Constants.UTM_CAMPAIGN, "banners");
//		utmMap.put(Constants.UTM_CONTENT,
//				Utils.templateIdToDisplay.get(templateID));

//		String apiKey = "99533a732f54096d7a0d1e0ad72ad923292f1933";
//		String segmentApiKey = "cwoDk4KQJFdunNYkGcS6C6CGjrAfKFxH";
		
//		pushEvent("aq-testing@gmail.com", Constants.END_ABILITY_QUIZ, utmMap,
//				apiKey,segmentApiKey);
	}

	private static final String BASE_KM_URL = "http://trk.kissmetrics.com/e";
	private static final String PERSON_PARAM = "_p";
	private static final String EVENT_PARAM = "_n";
	


	public static void pushEvent(String userName,
			String eventName, Map<String,String> utmMap,
			String apiKey, String segmentApiKey) {
		String url = null;;
			
		Map<String, String> propertiesMap = buildProperties(userName,eventName,utmMap);
		Analytics analytics = Analytics.builder(segmentApiKey).build();
		analytics.enqueue(TrackMessage.builder(eventName)
			    .userId(userName)
			);
		analytics.enqueue(IdentifyMessage.builder()
			    .userId(userName)
			    .traits(propertiesMap)
			);
		try {
			url = buildKMUrl(userName,eventName,utmMap,apiKey);
			sendGet(url);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static Map<String,String> buildProperties(String userName,
			String eventName, Map<String,String> utmMap) {
		
//		String channel = utmMap.get(Constants.UTM_CHANNEL);
//		String content = utmMap.get(Constants.UTM_CONTENT);
		
		Map<String, String> propertiesMap = new HashMap<String, String>();
		
		
		if(END_DIAGNOSTIC.equals(eventName) || END_QUALIFYING.equals(eventName)) {
			propertiesMap.put("Result", utmMap.get("Result"));
			propertiesMap.put("Percentage", utmMap.get("Percentage"));
			
		}
		
		return propertiesMap;
		
		
	}
	

	private static String buildKMUrl(String userName, String eventName,
			Map<String, String> utmMap, String apiKey)
			throws UnsupportedEncodingException {

		
		StringBuilder b = new StringBuilder();
		String epoch = utmMap.get("epoch");

		b.append(BASE_KM_URL);
		b.append("?").append("_k").append("=").append(apiKey);
		b.append("&").append(PERSON_PARAM).append("=").append(userName);
		b.append("&").append(EVENT_PARAM).append("=")
				.append(URLEncoder.encode(eventName, "UTF-8"));
		if(utmMap.containsKey("endTime")) {
			
			epoch = utmMap.get("endTime");
		}
		else {
			epoch = utmMap.get("startTime");
		}
		System.out.println("setting time stamp as "+epoch);
		b.append("&").append("_t").append("=").append(epoch);
		
		String percent = utmMap.get("Percentage");
		String passFailTag = utmMap.get("Result");
		if(eventName.equals(END_DIAGNOSTIC) ||   eventName.equals(END_QUALIFYING)) {
			b.append("&").append("Score").append("=")
			.append(percent);
			b.append("&").append("Result").append("=")
			.append(passFailTag);
	
		}
		
		String url = b.toString();
		try {
			URLEncoder.encode(url, "UTF-8");
			return url;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			
		}
		return url;
	}

	// HTTP GET request
	private static void sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		// con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());

	}
}
