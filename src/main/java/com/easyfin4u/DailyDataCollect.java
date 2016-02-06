package com.easyfin4u;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;



/**
 * This program collects daily data from Yahoo finance API and inserts to Mongo DB.
 * Batch should run on 10 AM PST everyday.
 * @author Sandeep Saini
 * 
 */
public class DailyDataCollect {
	
	static MongoClient mongo = new MongoClient(
			  new MongoClientURI( "mongodb://easyadmin:easyadmin_101@localhost:27017/easyfinDB" )
			);
	@SuppressWarnings("deprecation")
	static DBCollection table = mongo.getDB("easyfinDB").getCollection("historicalstocks");
	
	private final static String USER_AGENT = "Mozilla/5.0";
	static JSONParser parser1 = new JSONParser();
	static Calendar cal1 = Calendar.getInstance();
	static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
	private static final Logger logger = Logger.getLogger(DailyDataCollect.class.getName());
	
	
	public static void main(String args[]){

		JSONParser parser = new JSONParser();
		String symbol = null;
		ClassLoader classLoader = DailyDataCollect.class.getClassLoader();
		try {
			InputStream in = classLoader.getResourceAsStream("bse_symbol.json");
			InputStreamReader inReader = new InputStreamReader(in);
			Object obj = parser.parse(inReader);
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray symbolList = (JSONArray) jsonObject.get("symbolList");
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = symbolList.iterator();					
            while (iterator.hasNext()) {
            	symbol = iterator.next().get("SYMBOL").toString();
                try{
							DailyDataCollect.getDailyEquityData(symbol);
							
							//break;					 
                }catch(Exception e){
                	System.out.println(e.getMessage());
                }
            }
			
		} catch (Exception e) {
			e.printStackTrace();		
		}
		try {
			InputStream in = classLoader.getResourceAsStream("nse_symbol.json");
			InputStreamReader inReader = new InputStreamReader(in);
			Object obj = parser.parse(inReader);
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray symbolList = (JSONArray) jsonObject.get("symbolList");
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = symbolList.iterator();				
            while (iterator.hasNext()) {
            	symbol = iterator.next().get("SYMBOL").toString();
                try{
							DailyDataCollect.getDailyEquityData(symbol);
							
							//break;					 
                }catch(Exception e){
                	System.out.println(e.getMessage());
                }
            }
			
		} catch (Exception e) {
			e.printStackTrace();		
		}
	
}
	
	public static void insertToMongo(DBCollection table,String symbol,String date, String dayLow, String dayHigh, String dayOpen,
				String dayClose, String dayAdj){		
		WriteResult wr = null;		
		BasicDBObject andQuery = new BasicDBObject();
		List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
		obj.add(new BasicDBObject("symbol", symbol));
		obj.add(new BasicDBObject("date", date));
		andQuery.put("$and", obj);
		
		/**** Update ****/
		// create a document to store key and value
		BasicDBObject document = new BasicDBObject();
		document.put("symbol", symbol);
		document.put("date", date);
		document.put("day_high", dayLow);
		document.put("day_low", dayHigh);
		document.put("day_open", dayOpen);
		document.put("day_close", dayClose);
		document.put("day_end_adjusted", dayAdj);
		wr = table.update(andQuery,document,true,true);
		//table.insert(document);
	}
	
	
	// HTTP GET request
		private static void getDailyEquityData(String symbol) throws Exception {
			sdf1.setTimeZone(TimeZone.getTimeZone("IST"));
			Date date = null;
			String strDate = null;
			String day_high = null;
			String day_low = null;
			String day_end_adjusted = null;
			
			String url = "http://finance.yahoo.com/webservice/v1/symbols/"+symbol+"/quote?format=json&view=detail";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);
			int responseCode = con.getResponseCode();
			//System.out.println("\nSending 'GET' request to URL : " + url);
			//System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			Object json = parser1.parse(response.toString());
			JSONObject jsonObject = (JSONObject) json;
			JSONObject list = (JSONObject)jsonObject.get("list");
			JSONArray resourceList = (JSONArray)list.get("resources");
			JSONObject resourceItem = (JSONObject)resourceList.get(0);
			JSONObject resource = (JSONObject)resourceItem.get("resource");
			JSONObject fields = (JSONObject)resource.get("fields");

			day_high = (String)fields.get("day_high");
			day_low = (String)fields.get("day_low");
			day_end_adjusted = (String)fields.get("price");
			
			date = sdf1.parse((String)fields.get("utctime"));
			strDate = sdf1.format(date);
			
			//print result
			logger.info("SYMBOL:"+symbol);
			logger.info("DATE:"+strDate);
			logger.info("PRICE:"+day_end_adjusted);
			
			//DailyDataCollect.insertToMongo(table,symbol,strDate,day_low,day_high,"","",day_end_adjusted);	

		}
	
	
}
