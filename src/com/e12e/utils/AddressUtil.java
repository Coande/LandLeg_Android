package com.e12e.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class AddressUtil {
	static String clientmac;
	static String clientip;

	/**
	 * 获取WiFi状态下的Mac地址
	 * @param context
	 * @return
	 */
	public static String getLocalMAC(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		clientmac = info.getMacAddress().toUpperCase(); // 获取mac地址
		return clientmac;
	}

	/**
	 * 获取WiFi状态下的IP地址
	 * @param context
	 * @return
	 */
	public static String getLocalIP(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		int ipAddress = info.getIpAddress(); // 获取ip地址
		clientip = intToIp(ipAddress);
		return clientip;
	}


	/**
	 * 将十六进制IP地址转换成十进制
	 * @param 十六进制的IP地址
	 * @return 十进制的IP地址，并加间隔符
	 */
	public static String intToIp(int i) {
		return (i & 0xFF)+"."+ ((i >> 8) & 0xFF)+ "."+ ((i >> 16) & 0xFF)+ "."+ ((i >> 24) & 0xFF) ;
	}

}
