package com.e12e.landleg;

import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.e12e.utils.AddressUtil;
import com.e12e.utils.HttpUtil;
import com.e12e.utils.MD5Util;

import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	protected static final int VERIFYCODEEXCEPTION = 0;
	protected static final int LOGINEXCEPTION = 1;
	protected static final int LOGINSUCCESS = 2;
	protected static final int LOGINFAILED = 3;
	private static final int NASIPEXCEPTION = 4;
	private static final int LOGOUTEXCEPTION = 5;
	private static final int LOGOUTSUCCESS = 6;
	private static final int LOGOUTFAILED = 7;
	protected static final int GETVERIFYCODEFAILED = 8;
	// ����UI���õ��ı���
	Handler handler;
	Message msg;

	// ������Ϣ����
	String username;
	String password;
	Boolean isUse;
	String clientip;
	String clientmac;
	String nasip;
	int time;

	EsurfingService esurfingService;
	SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ��preferencefragment��ʾ����
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		SettingFragment settingFragment = new SettingFragment();
		fragmentTransaction.replace(R.id.pref, settingFragment).commit();

		// ��ʼ��
		esurfingService = new EsurfingService();

		// ���½���
		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				String message = null;
				switch (msg.what) {
				case LOGINSUCCESS:
					message = "��¼�ɹ�";
					break;
				case LOGINFAILED:
					message = "��¼ʧ�ܣ�" + (String) msg.obj;
					break;
				case LOGINEXCEPTION:
					message="��¼ʱ�����쳣";
					break;
				case VERIFYCODEEXCEPTION:
					message = "��ȡ��֤��ʱ�����쳣";
					break;
				case NASIPEXCEPTION:
					message = "�Զ���ȡNASIPʱ�����쳣";
					break;
				case LOGOUTEXCEPTION:
					message = "�ǳ�ʱ�����쳣";
					break;
				case LOGOUTSUCCESS:
					message = "�ǳ��ɹ�";
					break;
				case LOGOUTFAILED:
					message = "�ǳ�ʧ�ܣ�" + (String) msg.obj;
					break;
				case GETVERIFYCODEFAILED:
					message = "��֤���ȡʧ�ܣ�" + (String) msg.obj;
					break;
				default:
					message = "����Ϣ����û�ж��壺" + msg.what;
					break;
				}
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
			};
		};

	}

	/**
	 * ��������ӡ���ťʱ�Ĳ���
	 * 
	 * @param v
	 *            �ð�ť
	 */
	public void doConnect(View v) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		username = sharedPreferences.getString("username", "");
		password = sharedPreferences.getString("password", "");
		isUse = sharedPreferences.getBoolean("isUse", false);
	//	Log.d("isUse-------->", isUse + "");

		// ������ø߼�����
		if (isUse) {
			nasip = sharedPreferences.getString("nasip", "");
			clientip = sharedPreferences.getString("clientip", "");
			clientmac = sharedPreferences.getString("clientmac", "");

			// ��ʱ������
			time = Integer.parseInt(sharedPreferences.getString("time", "30"));

			// ֱ����������д����Ϣ����¼
			Login();
		} else {
			// ���û�����ø߼����ã����Զ���ȡ�����Ϣ�ٽ��е�¼����
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						// ��ȡNASIP
						nasip = esurfingService.getNASIP();
						// Log.d("NASIP-------->", nasip);
					} catch (Exception e) {
						handler.sendEmptyMessage(NASIPEXCEPTION);
						return;
					}
					// ��ȡip��mac
					clientmac = AddressUtil.getLocalMAC(MainActivity.this);
					clientip = AddressUtil.getLocalIP(MainActivity.this);

					// �ص�Login
					Login();
				}
			}).start();

		}

	}

	/**
	 * ��Ϣ��ȡ�ɹ���ص��� ��¼����
	 */
	public void Login() {
		// �ٴγ�ʼ�����������
		esurfingService = new EsurfingService(username, password, nasip, clientip, clientmac);
		new Thread(new Runnable() {
			@Override
			public void run() {

				// ��ȡ��֤��
				String verifyCodeString = null;
				String verifyCode = null;

				try {
					verifyCodeString = esurfingService.getVerifyCodeString();
				} catch (Exception e) {
					handler.sendEmptyMessage(VERIFYCODEEXCEPTION);
					return;
				}

				JSONObject challengeJSONObject = null;
				try {
					challengeJSONObject = new JSONObject(verifyCodeString);
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}
				int rescode0 = challengeJSONObject.optInt("rescode");
				if (rescode0 == 0) {
					verifyCode = challengeJSONObject.optString("challenge");
				} else {
					msg = handler.obtainMessage();
					msg.what = GETVERIFYCODEFAILED;
					msg.obj = challengeJSONObject.optString("resinfo");
					handler.sendMessage(msg);
					return;
				}
				// ��¼
				String loginString = null;
				try {
					loginString = esurfingService.doLogin(verifyCode);
				} catch (Exception e) {
					handler.sendEmptyMessage(LOGINEXCEPTION);
					return;
				}

				JSONObject loginJSONObject = null;
				try {
					loginJSONObject = new JSONObject(loginString);
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}

				int rescode = loginJSONObject.optInt("rescode");
				if (rescode == 0) {
					handler.sendEmptyMessage(LOGINSUCCESS);
					// ��¼�ϴε�¼ʱʹ�õĵ�¼��Ϣ�����ڵǳ���
					sharedPreferences.edit().putBoolean("lastIsUse", isUse).putString("lastUserame", username)
							.putString("lastPassword", password).putString("lastNasip", nasip)
							.putString("lastClientip", clientip).putString("lastClientmac", clientmac).commit();
					
					//Сͳ�ƣ�
					Analytics();
					
				} else {
					String resinfo = loginJSONObject.optString("resinfo");
					msg = handler.obtainMessage();
					msg.what = LOGINFAILED;
					msg.obj = resinfo;
					handler.sendMessage(msg);
				}

			}
		}).start();
	}

	/**
	 * �˳���¼
	 */
	public void Logout() {
		// �ǳ�
		new Thread(new Runnable() {
			@Override
			public void run() {
				String logoutString = null;
				try {
					// ��ȡ�ϴε�¼ʱ������
					boolean lastIsUse = sharedPreferences.getBoolean("lastIsUse", false);
					String lastUsername = sharedPreferences.getString("lastUsername", "");
					String lastPassword = sharedPreferences.getString("lastPassword", "");
					String lastNasip = sharedPreferences.getString("lastNasip", "");
					String lastClientip = sharedPreferences.getString("lastClientip", "");
					String lastClientmac = sharedPreferences.getString("lastClientmac", "");
					esurfingService = new EsurfingService(lastUsername, lastPassword, lastNasip, lastClientip,
							lastClientmac);
					// �˳��ϴεĵ�¼
					logoutString = esurfingService.doLogout();
				} catch (Exception e) {
					handler.sendEmptyMessage(LOGOUTEXCEPTION);
					e.printStackTrace();
					return;
				}
				JSONObject logoutJSONObject = null;

				try {
					logoutJSONObject = new JSONObject(logoutString);
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}

				int rescode = logoutJSONObject.optInt("rescode");
				if (rescode == 0) {
					handler.sendEmptyMessage(LOGOUTSUCCESS);
				} else {
					msg = handler.obtainMessage();
					msg.obj = logoutJSONObject.opt("resinfo");
					msg.what = LOGOUTFAILED;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	/**
	 * ���˵�
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * ����˵��¼�
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.logout) {
			Logout();
		} else if (item.getItemId() == R.id.about) {
			TextView tv = new TextView(this);
			String msg = "<h1>LandLeg Android v1.0</h1>" + "LandLeg�����ȣ�������У԰�������ͻ��ˣ�����<b>���</b>�й����ŵ�����У԰���в�������"
					+ "���˰�׿������<a href=\"https://github.com/Coande/LandLeg_Login_Java\">����Java��</a>��ֲ����"
					+ "�������൱����ȫ��Դ����ϸ������ο���<b><a href=\"http://coande.github.io\">Coande�Ĳ���</a></b><br/><br/><h4>By Coande</h4>";
			tv.setText(Html.fromHtml(msg));
			tv.setMovementMethod(LinkMovementMethod.getInstance());
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(16);
			tv.setPadding(50, 50, 50, 50);
			new Builder(this).setView(tv).show();
		}
		return true;
	}
	
	
	/**
	 *˳�� ����Сͳ�ƣ���Ҫ�����~~
	 */
	public void Analytics(){
		try{
			String resString=HttpUtil.doGet("http://ip.taobao.com/service/getIpInfo.php", "ip=myip");
			JSONObject jsonObject2=new JSONObject(resString);
			int code=jsonObject2.optInt("code");
			String city = null;
			if(code==0){
				JSONObject jsonObject3=(JSONObject) jsonObject2.opt("data");
				city=(String) jsonObject3.opt("city");
				city=URLEncoder.encode(city, "utf-8");
				String param="uid="+MD5Util.MD5(AddressUtil.getLocalMAC(MainActivity.this))+"&city="+city+"&type=2";
				HttpUtil.doGet("http://s2.e12e.com:8080/Analytics/", param);
			}
		}catch(Exception e){
			//��Ҫ����~~~
		}
	}

	// �˳���ʾ
	boolean isExit = false;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isExit == false) {
				isExit = true; // ׼���˳�
				Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						isExit = false; // ȡ���˳�
					}
				}, 2000); // ���2������û�а��·��ؼ�����������ʱ��ȡ�����ղ�ִ�е�����

			} else {
				finish();
				System.exit(0);
			}
		}
		return true;
	}

}
