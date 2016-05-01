package com.e12e.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class AddressUtil {
	static String clientmac;
	static String clientip;

	/**
	 * ��ȡWiFi״̬�µ�Mac��ַ
	 * @param context
	 * @return
	 */
	public static String getLocalMAC(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		clientmac = info.getMacAddress().toUpperCase(); // ��ȡmac��ַ
		return clientmac;
	}

	/**
	 * ��ȡWiFi״̬�µ�IP��ַ
	 * @param context
	 * @return
	 */
	public static String getLocalIP(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		int ipAddress = info.getIpAddress(); // ��ȡip��ַ
		clientip = intToIp(ipAddress);
		return clientip;
	}


	/**
	 * ��ʮ������IP��ַת����ʮ����
	 * @param ʮ�����Ƶ�IP��ַ
	 * @return ʮ���Ƶ�IP��ַ�����Ӽ����
	 */
	public static String intToIp(int i) {
		return (i & 0xFF)+"."+ ((i >> 8) & 0xFF)+ "."+ ((i >> 16) & 0xFF)+ "."+ ((i >> 24) & 0xFF) ;
	}

}
