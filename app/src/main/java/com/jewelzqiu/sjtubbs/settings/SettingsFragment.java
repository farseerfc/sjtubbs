package com.jewelzqiu.sjtubbs.settings;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.ListView;

/**
 * Created by jewelzqiu on 6/8/14.
 */
public class SettingsFragment extends PreferenceFragment implements Utils.OnLoginLogoutListener {

    private Preference accountPref;

    private Preference loginPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        Preference preference = findPreference(getString(R.string.key_github));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        (String) preference.getSummary()));
                startActivity(intent);
                return true;
            }
        });

        preference = findPreference(getString(R.string.key_creator));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.creator_email));
                try {
//                    startActivity(Intent.createChooser(intent, getString(R.string.select_mail_app)));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {

                }
                return true;
            }
        });

        preference = findPreference(getString(R.string.key_pic_path));
        preference.setSummary(Utils.PIC_STORE_PATH);
//        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                Uri uri = Uri.parse(Utils.PIC_STORE_PATH);
//                intent.setDataAndType(uri, "image/*");
//                startActivity(intent);
//                return true;
//            }
//        });

        accountPref = findPreference(getString(R.string.key_account));
        accountPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (Utils.USER_ID == null) {
                    return true;
                }

                return true;
            }
        });
        loginPref = findPreference(getString(R.string.key_login));
        loginPref.setTitle(Utils.USER_ID == null ? R.string.login : R.string.logout);
        loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (Utils.USER_ID != null) {
                    Utils.logout(SettingsFragment.this);
                } else {
                    Utils.login(getActivity(), SettingsFragment.this);
                }
                return true;
            }
        });

        onLoginLogout();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
//        listView.setFitsSystemWindows(true);
        listView.setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            listView.setPadding(
                    (int) getResources().getDimension(R.dimen.activity_horizontal_margin),
                    config.getPixelInsetTop(true),
                    (int) getResources().getDimension(R.dimen.activity_horizontal_margin),
                    config.getPixelInsetBottom());
        }
    }

    @Override
    public void onLoginLogout() {
        if (Utils.USER_ID == null) {
            accountPref.setTitle(getString(R.string.not_logged_in));
            accountPref.setEnabled(false);
            loginPref.setTitle(getString(R.string.login));
        } else {
            accountPref.setTitle(Utils.USER_ID);
            accountPref.setEnabled(true);
            loginPref.setTitle(getString(R.string.logout));
        }
    }
}
