package com.e12e.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.content.SyncStatusObserver;

public class HttpUtil {
	public static String doGet(String url, String param) throws IOException {
        String result = "";
        BufferedReader in = null;
 
            String urlNameString = url + "?" + param;
            
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 建立实际的连接
            connection.connect();

            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),"utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            
            in.close();
            return result;
	}
	
	
	public static String doPost(String url,String param) throws Exception{
		URL realUrl=new URL(url);
		URLConnection conn=realUrl.openConnection();
		// 发送POST请求必须设置如下两行
        conn.setDoOutput(true);
        conn.setDoInput(true);
        
        // 获取URLConnection对象对应的输出流
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        
        // 发送请求参数
        out.print(param);
        
        // flush输出流的缓冲
        out.flush();
        
        // 定义BufferedReader输入流来读取URL的响应
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(),"utf-8"));
        String line="",result = "";
        
        while ((line = in.readLine()) != null) {
            result += line;
        }
        in.close();
        out.close();
    	return result;
	}
	
	 public static String getRedirectUrl(String urlString) throws Exception {  
	        HttpURLConnection conn = (HttpURLConnection) new URL(urlString)  
	                .openConnection();
	        conn.setInstanceFollowRedirects(false);  
	        return conn.getHeaderField("Location");  
	    }
	

	
}
