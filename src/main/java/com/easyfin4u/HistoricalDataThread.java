package com.easyfin4u;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class HistoricalDataThread extends Thread {
	@SuppressWarnings("resource")
	static MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
	static DB db = mongoClient.getDB("test");
	static DBCollection table = db.getCollection("historicalstocks");
	String strdate = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	JSONParser parser = new JSONParser();
	String symbol = null;
	String pathName = "/Users/Sandy/Documents/workspace/Easyfin4uUtil/";
	
	
	public HistoricalDataThread(String fileName){
		pathName = pathName + fileName;
	}
	
	public void run() {	   		
	   		
	   		try {
	   			//System.out.println("Running Thread Name: "+ this.currentThread().getName());

	   			Calendar from = Calendar.getInstance();
	   			Calendar to = Calendar.getInstance();
	   			from.add(Calendar.MONTH, -2); // 2 months 
	   			
	   			Object obj = parser.parse(new FileReader(pathName));

	   			JSONObject jsonObject = (JSONObject) obj;
	   			JSONArray symbolList = (JSONArray) jsonObject.get("symbolList");
	   			
	   			@SuppressWarnings("unchecked")
	   			Iterator<JSONObject> iterator = symbolList.iterator();
	               while (iterator.hasNext()) {
	                   
	               	symbol = iterator.next().get("SYMBOL").toString();
	                   try{
	                   		Stock google = YahooFinance.get(symbol, from, to, Interval.DAILY);
	   				
	   						for (HistoricalQuote temp : google.getHistory()) {
	   							
	   							if (temp.getDate() != null) {
	   								strdate = sdf.format(temp.getDate().getTime());
	   								}
	   							
	   							System.out.println(temp.getSymbol());
	   							System.out.println(strdate);
	   							
	   							HistoricalData.insertToMongo(temp.getSymbol(),strdate,temp.getLow().toPlainString(),temp.getHigh().toPlainString(),
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

	public static void main(String[] args) {
		   Thread t1 = new Thread(new HistoricalDataThread("bse_symbols1.json"));
		   Thread t2 = new Thread(new HistoricalDataThread("bse_symbols2.json"));
		   Thread t3 = new Thread(new HistoricalDataThread("bse_symbols3.json"));
		   Thread t4 = new Thread(new HistoricalDataThread("bse_symbols4.json"));
		   Thread t5 = new Thread(new HistoricalDataThread("bse_symbols5.json"));
		   Thread t6 = new Thread(new HistoricalDataThread("bse_symbols6.json"));
		   Thread t7 = new Thread(new HistoricalDataThread("bse_symbols7.json"));
		   Thread t8 = new Thread(new HistoricalDataThread("bse_symbols8.json"));
		   Thread t9 = new Thread(new HistoricalDataThread("bse_symbols9.json"));
		   Thread t10 = new Thread(new HistoricalDataThread("bse_symbols10.json"));
		   Thread t11 = new Thread(new HistoricalDataThread("bse_symbols11.json"));
		   Thread t12 = new Thread(new HistoricalDataThread("bse_symbols12.json"));
		   Thread t13 = new Thread(new HistoricalDataThread("bse_symbols13.json"));

		   t1.start();
		   t2.start();
		   t3.start();
		   t4.start();
		   t5.start();
		   t6.start();
		   t7.start();
		   t8.start();
		   t9.start();
		   t10.start();
		   t11.start();
		   t12.start();
		   t13.start();
//		   try {
//		       t2.join();
//		   } catch (InterruptedException e) {
//		       System.out.println(e.getMessage());
//		   }

	}

}