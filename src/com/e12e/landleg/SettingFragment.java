package com.e12e.landleg;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingFragment extends PreferenceFragment {
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        // ��xml�ļ�����ѡ��  
        addPreferencesFromResource(R.xml.preferences);
    }  
}
