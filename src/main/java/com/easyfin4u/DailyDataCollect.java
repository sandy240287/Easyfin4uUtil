package com.easyfin4u;


import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class DailyDataCollect {
	
	@SuppressWarnings("resource")
	static MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
	static DB db = mongoClient.getDB("test");
	static DBCollection table = db.getCollection("historicalstocks");

	public static void main(String args[]){

		String strdate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		JSONParser parser = new JSONParser();
		String symbol = null;
		Calendar cal = Calendar.getInstance();
		
		try {
			
			Object obj = parser.parse(new FileReader("/Users/ssaini/Documents/DSGWorkspace/Easyfin4uUtil/nasdaq_symbol.json"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray symbolList = (JSONArray) jsonObject.get("symbolList");
			
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = symbolList.iterator();
			
			strdate = sdf.format(cal.getTime());
			
            while (iterator.hasNext()) {
                
            	symbol = iterator.next().get("SYMBOL").toString();
                try{
                		Stock stock = YahooFinance.get(symbol);
                		StockQuote sq = stock.getQuote();
                		
							/*System.out.println(iterator.next().get("SYMBOL"));
							System.out.println(strdate);
							System.out.println(sq.getDayLow().toPlainString());
							System.out.println(sq.getDayHigh().toPlainString());
							System.out.println(sq.getOpen().toPlainString());
							System.out.println(sq.getPreviousClose().toPlainString());
							System.out.println(sq.getPrice().toPlainString());*/
							
						HistoricalData.insertToMongo(symbol,strdate,sq.getDayLow().toPlainString(),sq.getDayHigh().toPlainString(),
								sq.getOpen().toPlainString(),sq.getPreviousClose().toPlainString(),sq.getPrice().toPlainString());
							
							break;
						
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
