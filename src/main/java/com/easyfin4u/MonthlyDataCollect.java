package com.easyfin4u;


import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MonthlyDataCollect {
	
	@SuppressWarnings("resource")
	static MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
	static DB db = mongoClient.getDB("test");
	static DBCollection table = db.getCollection("monthly_historicalstocks");

	public static void main(String args[]){

		String strdate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		JSONParser parser = new JSONParser();
		String symbol = null;
		
		try {

			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();
			from.add(Calendar.YEAR, -1); // from 5 years ago 
			
			Object obj = parser.parse(new FileReader("/Users/ssaini/Documents/DSGWorkspace/Easyfin4uUtil/nasdaq_symbol.json"));

			JSONObject jsonObject = (JSONObject) obj;
			JSONArray symbolList = (JSONArray) jsonObject.get("symbolList");
			
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = symbolList.iterator();
            while (iterator.hasNext()) {
                
            	symbol = iterator.next().get("SYMBOL").toString();
                try{
                		Stock google = YahooFinance.get(symbol, from, to, Interval.MONTHLY);
				
						for (HistoricalQuote temp : google.getHistory()) {
							
							if (temp.getDate() != null) {
								strdate = sdf.format(temp.getDate().getTime());
								}
							
							/*System.out.println(symbol);
							System.out.println(strdate);
							System.out.println(temp.getLow());
							System.out.println(temp.getHigh());
							System.out.println(temp.getOpen());
							System.out.println(temp.getClose());
							System.out.println(temp.getAdjClose());*/
							
							MonthlyDataCollect.insertToMongo(symbol,strdate,temp.getLow().toPlainString(),temp.getHigh().toPlainString(),
									temp.getOpen().toPlainString(),temp.getClose().toPlainString(),temp.getAdjClose().toPlainString());
							
							//break;
						}
                //break;    
                }catch(Exception e){
                	System.out.println(e.getMessage());
                }
            }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}		
	}
	
	public static void insertToMongo(String symbol,String date, String dayLow, String dayHigh, String dayOpen,
				String dayClose, String dayAdj){
		
		/**** Insert ****/
		// create a document to store key and value
		BasicDBObject document = new BasicDBObject();
		document.put("symbol", symbol);
		document.put("date", date);
		document.put("day_high", dayLow);
		document.put("day_low", dayHigh);
		document.put("day_open", dayOpen);
		document.put("day_close", dayClose);
		document.put("day_end_adjusted", dayAdj);
		table.insert(document);
		
	}
	
	
}
