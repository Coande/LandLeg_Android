package com.e12e.landleg;

import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.json.JSONObject;

import com.e12e.utils.HttpUtil;
import com.e12e.utils.MD5Util;

public class EsurfingService {
	String username;
	String password;
	String nasip;
	String clientip;
	String clientmac;
	
	String secret = "Eshore!@#";
	String iswifi = "4060";

	String timestamp;
	String md5String;
	String url;
	
	public EsurfingService(){
		
	}
	
	
	public EsurfingService(String username,String password,String nasip,String clientip,String clientmac){
		this.username=username;
		this.password=password;
		this.nasip=nasip;
		this.clientip=clientip;
		this.clientmac=clientmac;
	}


	/**
	 * �õ���¼ʱ��Ҫ����֤������
	 * 
	 * @return ���ص�¼ʱ��Ҫ����֤������
	 * @throws Exception
	 */
	public String getVerifyCodeString() throws Exception {
		url = "http://enet.10000.gd.cn:10001/client/challenge";
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + clientmac + timestamp + secret);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", username);
		jsonObject.put("clientip", clientip);
		jsonObject.put("nasip", nasip);
		jsonObject.put("mac", clientmac);
		jsonObject.put("iswifi", iswifi);
		jsonObject.put("timestamp", timestamp);
		jsonObject.put("authenticator", md5String);

		String verifyCodeString = HttpUtil.doPost(url, jsonObject.toString());
		return verifyCodeString;
	}

	/**
	 * ��¼
	 * 
	 * @param ��֤��
	 * @return ��¼ʱ���������ص�����
	 * @throws Exception
	 */
	public String doLogin(String verifyCode) throws Exception {
		url = "http://enet.10000.gd.cn:10001/client/login";
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + clientmac + timestamp + verifyCode
				+ secret);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", username);
		jsonObject.put("password", password);
		jsonObject.put("verificationcode", "");
		jsonObject.put("clientip", clientip);
		jsonObject.put("nasip", nasip);
		jsonObject.put("mac", clientmac);
		jsonObject.put("iswifi", iswifi);
		jsonObject.put("timestamp", timestamp);
		jsonObject.put("authenticator", md5String);

		String loginString = HttpUtil.doPost(url, jsonObject.toString());

		return loginString;
	}

	/**
	 * �ǳ�
	 * 
	 * @return �ǳ�ʱ���������ص�����
	 * @throws Exception
	 */
	public String doLogout() throws Exception {
		url = "http://enet.10000.gd.cn:10001/client/logout";
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + clientmac + timestamp + secret);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("clientip", clientip);
		jsonObject.put("nasip", nasip);
		jsonObject.put("mac", clientmac);
		jsonObject.put("timestamp", timestamp);
		jsonObject.put("authenticator", md5String);

		String logoutString = HttpUtil.doPost(url, jsonObject.toString());

		return logoutString;
	}

	/**
	 * ����ά����������
	 * 
	 * @return ���ؽ��
	 * @throws Exception
	 */
	public String doActive() throws Exception {
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + clientmac + timestamp + secret);
		url = "http://enet.10000.gd.cn:8001/hbservice/client/active";

		// ��֪��ΪɶtimestampΪnullʱ����ͨ��~
		String param = "username=" + username + "&clientip=" + clientip
				+ "&nasip=" + nasip + "&mac=" + clientmac + "&timestamp=" + timestamp
				+ "&authenticator=" + md5String;
		String activeString = HttpUtil.doGet(url, param);
		return activeString;
	}
	
	
	/**
	 * ���ʰٶ������Ի�ȡNASIP
	 * @return ��ȡ����NASIP
	 * @throws Exception
	 */
	public String getNASIP() throws Exception {
		String nasip = null;
		String location = HttpUtil.getRedirectUrl("http://www.baidu.com");
		if (location != null) {
			URL url = new URL(location);
			String queryString = url.getQuery();
			if (queryString != null) {
				String[] queryStringArr = queryString.split("&");
				HashMap<String, String> map = new HashMap<String, String>();
				for (String query : queryStringArr) {
					String[] querStringArry2 = query.split("=");
					map.put(querStringArry2[0], querStringArry2[1]);
				}
				nasip = map.get("wlanacip");
			}
		}
		return nasip;
	}
	


}
