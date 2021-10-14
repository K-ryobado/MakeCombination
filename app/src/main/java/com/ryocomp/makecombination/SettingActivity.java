package com.ryocomp.makecombination;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity {
    private TestOpenHelper helper;
    private SQLiteDatabase db;
    private String table = "main";
    private String table2 = "setting";
    private List<String> db_member_list = new ArrayList<String>();
    private List<String> db_sex_list = new ArrayList<String>();
    private List<String> db_pair_list = new ArrayList<String>();
    private List<String> db_rest_list = new ArrayList<String>();
    private List<Spinner> restMemberSpinner_list = new ArrayList<Spinner>();
    private List<Spinner> rest_matchSpinner_list = new ArrayList<Spinner>();
    private List<Spinner> pairLeftSpinner_list = new ArrayList<Spinner>();
    private List<Spinner> pairRightSpinner_list = new ArrayList<Spinner>();
    private String courtNumber_db;
    private String mixOnly_db;
    private Boolean mixOnly = false;
    private String[] spinnerItems_valid = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private String[] rest_motion;
    private String sqlc_rest = "";
    private String sqlc_game = "";
    private String sqlc_free = "";
    private String sqlc_pair = "";
    private String sqlc_pair_free = "";
    private boolean isRest_match_setting = false;
    private boolean isFixedPair_setting = false;
    private ArrayAdapter<String> rest_motionAdapter;
    private ArrayAdapter<String> membersAdapter;
    private List<Integer> restMatchLinearIndex_list = new ArrayList<Integer>();
    LinearLayout restLinear;
    private List<Integer> pairLinearIndex_list = new ArrayList<Integer>();
    LinearLayout fixedLinear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // バナー広告の生成
        AdView adView = findViewById(R.id.adView);
        // テスト xmlで書く場合は不要
//        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        // 本番
//        adView.setAdUnitId("ca-app-pub-3891518799622736/1551389584");
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // 問い合わせボタン
        TextView contactButton = findViewById(R.id.contactButton);
        contactButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLScnmS-IRFWNA6kNRrLiT9LIOBiHa7kY9LEp_7sDKjIMmtjcrw/viewform?usp=sf_link"));
            startActivity(intent);
        });

        // DB取得
        if (helper == null) {
            helper = new TestOpenHelper(getApplicationContext());
        }
        if (db == null) {
            db = helper.getWritableDatabase();
        }

        //検索 table2-------------------------------------------
        try {
            Log.d("debug", "**********Cursor2");
            Cursor cursor2 = db.query(
                    table2,
                    new String[]{"courtNumber", "mixOnly"},
                    null,
                    null,
                    null,
                    null,
                    null
            );

            cursor2.moveToFirst();

            if (cursor2.getCount() > 0) {
                courtNumber_db = cursor2.getString(0);
                mixOnly_db = cursor2.getString(1);
                StringBuilder sbuilder2 = new StringBuilder();
                sbuilder2.append(" courtNumber： " + courtNumber_db);
                sbuilder2.append(" mixOnly： " + mixOnly_db);
                Log.d("debug", "**********" + sbuilder2.toString());
                if (mixOnly_db.equals("on")) {
                    mixOnly = true;
                }
            }

            // 忘れずに！
            cursor2.close();
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }//------------------------------------------------------

        //検索 table-------------------------------------------
        try {
            Log.d("debug", "**********Cursor");
            Cursor cursor = db.query(
                    table,
                    new String[]{"number", "member", "sex", "pair", "rest", "enable"},
                    null,
                    null,
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();

            db_member_list.add("");
            for (int i = 0; i < cursor.getCount(); i++) {
                Log.d("debug", "enable:" + cursor.getString(5));
                if (cursor.getString(5).equals("有効")) {
                    StringBuilder sbuilder = new StringBuilder();
                    sbuilder.append(cursor.getInt(0) + " ： " + cursor.getString(1) + " ： " + cursor.getString(2) + " ： " + cursor.getString(3) + " ： " + cursor.getString(4));
                    db_member_list.add(cursor.getString(1));
                    db_sex_list.add(cursor.getString(2));
                    db_pair_list.add(cursor.getString(3));
                    db_rest_list.add(cursor.getString(4));
                    Log.d("debug", "**********" + sbuilder.toString());

                    if (cursor.getString(3).length() > 0) {
                        isFixedPair_setting = true;
                    }
                    if (cursor.getString(4).length() > 0) {
                        isRest_match_setting = true;
                    }
                }

                cursor.moveToNext();
            }
            // 忘れずに！
            cursor.close();
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }//------------------------------------------------------

        // コート数************************************************************
        // スピナー-----------------------------------------
        Spinner courtSpinner = findViewById(R.id.courtNumber);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                spinnerItems_valid);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        courtSpinner.setAdapter(adapter);
        int spinnerPosition = 0;
        if (courtNumber_db != null) {
            spinnerPosition = adapter.getPosition(courtNumber_db);
        }
        Log.d("debug", "spinnerPosition：" + spinnerPosition);
        courtSpinner.setSelection(spinnerPosition);
        //-------------------------------------------------
        //*********************************************************************

        //混合ダブルスのみ******************************************************
//        TextView mixOnly_text = findViewById(R.id.mixOnly_str);
//        CheckBox mixCheckBox = findViewById(R.id.mixCheckBox);
//        int manCount = 0;
//        int womanCount = 0;
//        for (String s : db_sex_list) {
//            if (s.equals("女性")) {
//                womanCount++;
//            }
//            if (s.equals("男性")) {
//                manCount++;
//            }
//        }
//        if (manCount < 2 || womanCount < 2) {
//            mixOnly_text.setEnabled(false);
//            mixCheckBox.setEnabled(false);
//        }
//        Log.d("debug", "mixOnly[boolean]：" + mixOnly);
//        mixCheckBox.setChecked(mixOnly);
        //*********************************************************************

        //休憩・試合設定*******************************************************************
        //
        CheckBox restCheckbox = findViewById(R.id.restCheckBox);
        LinearLayout rest_match_setting_linear = findViewById(R.id.rest_match_setting_linear);
        if (isRest_match_setting) {
            rest_match_setting_linear.setVisibility(View.VISIBLE);
            restCheckbox.setChecked(true);
        }
        restCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    //チェックが入った時の処理
                    rest_match_setting_linear.setVisibility(View.VISIBLE);
                } else {
                    //チェックが外れた時の処理
                    rest_match_setting_linear.setVisibility(View.GONE);
                }
            }
        });

        restLinear = findViewById(R.id.restLinear);
        // adapterの作成
        rest_motion = new String[]{getResources().getString(R.string.rest_str), getResources().getString(R.string.motion_str)};
        rest_motionAdapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                rest_motion);
        rest_motionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        membersAdapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                db_member_list);
        membersAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // DBの値を反映
        int index = 0;
        for (int i = 0; i < db_rest_list.size(); i++) {
            String name = db_member_list.get(i + 1).toString();
            String rest = db_rest_list.get(i).toString();
            Log.d("debug", "name：" + name);
            Log.d("debug", "rest：" + rest);
            if (!rest.equals("")) {
                int memberPosition = membersAdapter.getPosition(name);
                int restPosition = rest_motionAdapter.getPosition(rest);
                // 追加していく処理
                // レイアウト生成----------------------------------
                LinearLayout layout_horizontal_add = MakeRestMatchLinear();
                restLinear.addView(layout_horizontal_add);
                AddRestMatchElement(index, layout_horizontal_add, memberPosition, restPosition);
                index++;
//                }
            }
        }
        // 1つもなかったら空で作成
        Log.d("debug", "restLinear.getChildCount()：" + restLinear.getChildCount());
        if (restLinear.getChildCount() == 0) {
            LinearLayout layout_horizontal_add = MakeRestMatchLinear();
            restLinear.addView(layout_horizontal_add);
            AddRestMatchElement(0, layout_horizontal_add, 0, 0);
        }


        // 固定ペア*************************************************************
        CheckBox pairCheckBox = findViewById(R.id.pairCheckBox);
        LinearLayout fixedPairs_setting_linear = findViewById(R.id.fixedPairs_setting_linear);
        if (isFixedPair_setting) {
            fixedPairs_setting_linear.setVisibility(View.VISIBLE);
            pairCheckBox.setChecked(true);
        }
        pairCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    //チェックが入った時の処理
                    fixedPairs_setting_linear.setVisibility(View.VISIBLE);
                } else {
                    //チェックが外れた時の処理
                    fixedPairs_setting_linear.setVisibility(View.GONE);
                }
            }
        });
        // レイアウト
        fixedLinear = findViewById(R.id.fixedLinear);

        // adapterの作成
        membersAdapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                db_member_list);
        membersAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // DBの値を反映
        int pairIndex = 0;
        for (int i = 0; i < db_pair_list.size(); i++) {
            String pair = db_pair_list.get(i);
            String leftName = db_member_list.get(i + 1);
            String rightName = "";
            Log.d("debug", "pair：" + pair);
            Log.d("debug", "leftName：" + leftName);
            if (!pair.equals("")) {
                String pairNumber = pair.substring(0, pair.length() - 1);
                Log.d("debug","pairNumber："+pairNumber);
                if (pair.indexOf("L") > -1) {
                    int rightIndex = 0;
                    for (int p=0; p < db_pair_list.size(); p++){
                        Log.d("debug","db_pair_list.get(p)："+db_pair_list.get(p));
                        Log.d("debug","db_pair_list.get(p).equals(pairNumber+\"R\")："+db_pair_list.get(p).equals(pairNumber+"R"));
                        if (db_pair_list.get(p).equals(pairNumber+"R")){
                            rightIndex = p+1;
                            break;
                        }
                    }
                    Log.d("debug","rightIndex："+rightIndex);
                    if (rightIndex > -1) {
                        rightName = db_member_list.get(rightIndex);
                        int leftPosition = membersAdapter.getPosition(leftName);
                        int rightPosition = membersAdapter.getPosition(rightName);
                        Log.d("debug","leftPosition："+leftPosition+"　rightPosition："+rightPosition);
                        // 追加していく処理
                        // レイアウト生成----------------------------------
                        LinearLayout layout_horizontal_add = MakePairLinear();
                        fixedLinear.addView(layout_horizontal_add);
                        AddPairElement(pairIndex, layout_horizontal_add, leftPosition, rightPosition);
                        pairIndex++;
                    }
                }
            }
        }
        // 1つもなかったら空で作成
        Log.d("debug", "fixedLinear.getChildCount()：" + fixedLinear.getChildCount());
        if (fixedLinear.getChildCount() == 0) {
            LinearLayout layout_horizontal_add = MakePairLinear();
            fixedLinear.addView(layout_horizontal_add);
            AddPairElement(0, layout_horizontal_add, 0, 0);
        }

        // メンバー設定画面へ********************************************************************
        Button memberSettingButton = findViewById(R.id.memberButton);
        memberSettingButton.setOnClickListener(v ->

        {
            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(intent);
        });
        //*************************************************************************************

        //組み合わせ作成画面へ******************************************************************
        Button makeCombinationButton = findViewById(R.id.makeButton);
        makeCombinationButton.setOnClickListener(v ->
        {
            //コート数
            String courtNumber = courtSpinner.getSelectedItem().toString();
            // 混合ダブルスのみ
            String mixOnly_str="";
//            if (mixCheckBox.isChecked()) {
//                mixOnly_str = "on";
//            } else {
//                mixOnly_str = "off";
//            }


            // 休憩・試合メンバー取得
            ArrayList<String> restMembers = new ArrayList<String>();
            ArrayList<String> gameMember = new ArrayList<String>();
            if (restCheckbox.isChecked()) {
                for (int i = 0; i < restMemberSpinner_list.size(); i++) {
                    if (restMemberSpinner_list.get(i).getSelectedItemPosition() > 0) {
                        if (rest_matchSpinner_list.get(i).getSelectedItem().toString().equals("休憩")) {
                            //休憩
                            restMembers.add(restMemberSpinner_list.get(i).getSelectedItem().toString());
                        } else {
                            //試合
                            gameMember.add(restMemberSpinner_list.get(i).getSelectedItem().toString());
                        }
                    }
                }
                //有効メンバー から 休憩メンバーを差し引いて4人未満ならアラート
                Log.d("debug","db_member_list.size()："+db_member_list.size()+"　restMembers.size()："+restMembers.size());
                if (db_member_list.size()-restMembers.size()-1 < 4){
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.alert)
                            .setMessage(R.string.msg3)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
            }

            // 固定ペア取得
            ArrayList<String> fixedMembersL = new ArrayList<String>();
            ArrayList<String> fixedMembersR = new ArrayList<String>();
            if (pairCheckBox.isChecked()) {
                for (int i = 0; i < pairLeftSpinner_list.size(); i++) {
                    if (pairLeftSpinner_list.get(i).getSelectedItemPosition() > 0 && pairRightSpinner_list.get(i).getSelectedItemPosition() > 0) {
                        String left_str = pairLeftSpinner_list.get(i).getSelectedItem().toString();
                        String right_str = pairRightSpinner_list.get(i).getSelectedItem().toString();
                        if (restMembers.contains(left_str) || restMembers.contains(right_str)) {
                            String containMember = "";
                            if (restMembers.contains(left_str)) {
                                containMember = left_str;
                            } else if (restMembers.contains(right_str)) {
                                containMember = right_str;
                            }
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.alert)
                                    .setMessage(containMember + R.string.msg4)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        } else {
                            if (left_str == right_str) {
                                new AlertDialog.Builder(this)
                                        .setTitle(R.string.alert)
                                        .setMessage("「" + left_str + "」さんの固定ペアが「" + right_str + "」になっています。\n同一人物を固定ペアに設定することはできません。")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton("OK", null)
                                        .show();
                                return;
                            }
                            //重複確認
                            for (int ii = 0; ii < pairLeftSpinner_list.size(); ii++){
                                if (i == ii){
                                    continue;
                                }
                                if(left_str == pairLeftSpinner_list.get(ii).getSelectedItem().toString() || left_str == pairRightSpinner_list.get(ii).getSelectedItem().toString()){
                                    new AlertDialog.Builder(this)
                                            .setTitle("注意")
                                            .setMessage("「" + left_str + "」さんが複数設定されています。")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton("OK", null)
                                            .show();
                                    return;
                                }

                                if(right_str == pairLeftSpinner_list.get(ii).getSelectedItem().toString() || right_str == pairRightSpinner_list.get(ii).getSelectedItem().toString()){
                                    new AlertDialog.Builder(this)
                                            .setTitle("注意")
                                            .setMessage("「" + right_str + "」さんが複数設定されています。")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton("OK", null)
                                            .show();
                                    return;
                                }
                            }
                            //L
                            fixedMembersL.add(left_str);
                            //R
                            fixedMembersR.add(right_str);
                        }
                    }
                }

            }

            // 休憩
            String restMembers_str = "";
            if (restMembers.size() == 0) {
                restMembers_str = "―";
            } else {
                restMembers_str = Integer.toString(restMembers.size()) + "人";
                for (String str : restMembers) {
                    restMembers_str = restMembers_str + "\n　　「" + str + "」";
                    sqlc_rest = sqlc_rest + "UPDATE " + table + " SET rest='休憩' WHERE member='" + str + "';@@";
                }
            }
            Log.d("debug", "sqlc_rest：" + sqlc_rest);
            //　試合
            String gameMembers_str = "";
            if (gameMember.size() == 0) {
                gameMembers_str = "―";
            } else {
                gameMembers_str = Integer.toString(gameMember.size()) + "人";
                for (String str : gameMember) {
                    gameMembers_str = gameMembers_str + "\n　　「" + str + "」";
                    sqlc_game = sqlc_game + "UPDATE " + table + " SET rest='試合' WHERE member='" + str + "';@@";
                }
            }
            Log.d("debug", "sqlc_game：" + sqlc_game);
            // 指定なし
            String free = sqlc_game + sqlc_rest;
            for (String str : db_member_list) {
                if (!free.contains(str)) {
                    sqlc_free = sqlc_free + "UPDATE " + table + " SET rest='' WHERE member='" + str + "';@@";
                }
            }

            // 固定ペア
            String fixedPairs_str = "";
            if (fixedMembersL.size() == 0) {
                fixedPairs_str = "―";
            } else {
                // 一旦削除
                String sqlc_columnDelete = "UPDATE " + table + " SET pair='' WHERE pair != '';";
                db.execSQL(sqlc_columnDelete);
                fixedPairs_str = Integer.toString(fixedMembersL.size()) + "ペア";
                for (int i = 0; i < fixedMembersL.size(); i++) {
                    fixedPairs_str = fixedPairs_str + "\n　　「" + fixedMembersL.get(i).toString() + "・" + fixedMembersR.get(i).toString() + "」";
                    sqlc_pair = sqlc_pair + "UPDATE " + table + " SET pair='" + Integer.toString(i + 1) + "L' WHERE member='" + fixedMembersL.get(i).toString() + "';@@";
                    sqlc_pair = sqlc_pair + "UPDATE " + table + " SET pair='" + Integer.toString(i + 1) + "R' WHERE member='" + fixedMembersR.get(i).toString() + "';@@";
                }
            }
            //指定なし
            for (String str : db_member_list) {
                if (!sqlc_pair.contains(str)) {
                    sqlc_free = sqlc_free + "UPDATE " + table + " SET pair='' WHERE member='" + str + "';@@";
                }
            }

            Log.d("debug", "sqlc_pair：" + sqlc_pair);

            new AlertDialog.Builder(this)
                    .setTitle("設定確認")
                    .setMessage("コート数：" + courtNumber + "面"
                            + "\n混合ダブルスのみ：" + mixOnly_str
                            + "\n休憩：" + restMembers_str
                            + "\n試合：" + gameMembers_str
                            + "\n固定ペア：" + fixedPairs_str
                            + "\n\nこの内容で組み合わせを作成します。\nよろしいですか？")
                    .setIcon(R.drawable.question)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // 現在のテーブルデータを削除
                            db.delete(table2, null, null);
                            // 登録-------------------------------------------------
                            InsertData(db, table2, courtNumber, mixOnly_str);
                            //更新
                            if (sqlc_rest.length() > 0) {
                                String[] ary = sqlc_rest.split("@@");
                                for (String str : ary) {
                                    if (str.length() > 0) {
                                        db.execSQL(str);
                                        Log.d("debug", str);
                                    }
                                }
                            }

                            if (sqlc_game.length() > 0) {
                                String[] ary = sqlc_game.split("@@");
                                for (String str : ary) {
                                    if (str.length() > 0) {
                                        db.execSQL(str);
                                        Log.d("debug", str);
                                    }
                                }
                            }

                            if (sqlc_free.length() > 0) {
                                String[] ary = sqlc_free.split("@@");
                                for (String str : ary) {
                                    if (str.length() > 0) {
                                        db.execSQL(str);
                                        Log.d("debug", str);
                                    }
                                }
                            }

                            if (sqlc_pair.length() > 0) {
                                String[] ary = sqlc_pair.split("@@");
                                for (String str : ary) {
                                    if (str.length() > 0) {
                                        db.execSQL(str);
                                        Log.d("debug", str);
                                    }
                                }
                            }

                            // 画面遷移
                            Intent intent = new Intent(SettingActivity.this, MakeCombActivity.class);
                            intent.putExtra("restChecked",restCheckbox.isChecked());
                            intent.putExtra("pairChecked",pairCheckBox.isChecked());
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    })
                    .show();

        });
        //*************************************************************************************
    }

    private void InsertData(SQLiteDatabase db, String table, String courtNumber, String mixOnly) {
        ContentValues values = new ContentValues();
        values.put("courtNumber", courtNumber);
        values.put("mixOnly", mixOnly);
        db.insert(table, null, values);
    }

    private void ChangeSpinner(ArrayList spinners) {

    }

    //dpをpxに置換
    public static int convertDpToPx(Context context, int dp) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * d) + 0.5);
    }

    private LinearLayout MakeRestMatchLinear() {
        LinearLayout layout_horizontal_add = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                convertDpToPx(this, 35));
        layout_horizontal_add.setOrientation(LinearLayout.HORIZONTAL);
        layout_horizontal_add.setLayoutParams(lp);
        layout_horizontal_add.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        layout_horizontal_add.setBackgroundResource(R.drawable.border);
        return layout_horizontal_add;
    }

    private void AddRestMatchElement(int index, LinearLayout layout_horizontal_add, int memberPosition, int restPosition) {
        LinearLayout.LayoutParams addLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // indexリストの作成
        restMatchLinearIndex_list.add(index);
        // 削除ボタン
        ImageView restDeleteButton = new ImageView(this);
        restDeleteButton.setImageResource(R.drawable.icon_minus_32);
        layout_horizontal_add.addView(restDeleteButton, new LinearLayout.LayoutParams(convertDpToPx(this, 25), convertDpToPx(this, 25)));
        // メンバーのスピナー--------------------------------
        Spinner restMemberSpinner = new Spinner(this);
        restMemberSpinner_list.add(restMemberSpinner);
        restMemberSpinner.setAdapter(membersAdapter);
        restMemberSpinner.setSelection(memberPosition);
        layout_horizontal_add.addView(restMemberSpinner, addLp);
        Log.d("debug", "restMemberSpinner追加　index：" + index);
        // コロン
        TextView colon = new TextView(this);
        colon.setText("：");
        colon.setTypeface(Typeface.DEFAULT_BOLD);
        layout_horizontal_add.addView(colon, addLp);
        // 休憩・試合のスピナー--------------------------------
        Spinner rest_matchSpinner = new Spinner(this);
        rest_matchSpinner_list.add(rest_matchSpinner);
        rest_matchSpinner.setAdapter(rest_motionAdapter);
        rest_matchSpinner.setSelection(restPosition);
        layout_horizontal_add.addView(rest_matchSpinner, addLp);
        //-------------------------------------------------

        // 削除ボタン
        restDeleteButton.setOnClickListener(v -> {
            Log.d("debug", "index：" + index);
            Log.d("debug", "restLinear.getChildCount()：" + restLinear.getChildCount());
            Log.d("debug", "restMemberSpinner_list.size()：" + restMemberSpinner_list.size());
            Log.d("debug", "rest_matchSpinner_list.size()：" + rest_matchSpinner_list.size());
            Log.d("debug", "restMatchLinearIndex_list.indexOf(index)：" + restMatchLinearIndex_list.indexOf(index));
            if (restLinear.getChildCount() > 1) {
                restLinear.removeViewAt(restMatchLinearIndex_list.indexOf(index));
                restMemberSpinner_list.remove(restMatchLinearIndex_list.indexOf(index));
                rest_matchSpinner_list.remove(restMatchLinearIndex_list.indexOf(index));
                restMatchLinearIndex_list.remove(restMatchLinearIndex_list.indexOf(index));
            }else {
                restMemberSpinner.setSelection(0);
                rest_matchSpinner.setSelection(0);
            }
        });
        // メンバーのスピナー
        restMemberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }

            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                //選択されたアイテム名と位置（index)を内部変数へ保存
                String name = parent.getSelectedItem().toString();
                Log.d("debug", "name：" + name + "　position：" + position);
                Log.d("debug", "restMatchLinearIndex_list.indexOf(index)：" + restMatchLinearIndex_list.indexOf(index) + "　restLinear.getChildCount()-1：" + restLinear.getChildCount());
                if (position > 0 && restMatchLinearIndex_list.indexOf(index) == restLinear.getChildCount() - 1) {
                    // 追加
                    LinearLayout layout_horizontal_add = MakeRestMatchLinear();
                    restLinear.addView(layout_horizontal_add);
                    AddRestMatchElement(restLinear.getChildCount() - 1, layout_horizontal_add, 0, 0);
                }
            }
        });
    }

    private LinearLayout MakePairLinear() {
        LinearLayout layout_horizontal_add = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                convertDpToPx(this, 35));
        layout_horizontal_add.setOrientation(LinearLayout.HORIZONTAL);
        layout_horizontal_add.setLayoutParams(lp);
        layout_horizontal_add.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        layout_horizontal_add.setBackgroundResource(R.drawable.border);
        return layout_horizontal_add;
    }

    private void AddPairElement(int index, LinearLayout layout_horizontal_add, int leftPosition, int rightPosition) {
        LinearLayout.LayoutParams addLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // indexリストの作成
        pairLinearIndex_list.add(index);
        // 削除ボタン
        ImageView pairDeleteButton = new ImageView(this);
        pairDeleteButton.setImageResource(R.drawable.icon_minus_32);
        layout_horizontal_add.addView(pairDeleteButton, new LinearLayout.LayoutParams(convertDpToPx(this, 25), convertDpToPx(this, 25)));
        // メンバー左のスピナー--------------------------------
        Spinner leftMemberSpinner = new Spinner(this);
        pairLeftSpinner_list.add(leftMemberSpinner);
        leftMemberSpinner.setAdapter(membersAdapter);
        leftMemberSpinner.setSelection(leftPosition);
        layout_horizontal_add.addView(leftMemberSpinner, addLp);
        Log.d("debug", "leftMemberSpinner追加　index：" + index);
        // メンバー右のスピナー--------------------------------
        Spinner rightMemberSpinner = new Spinner(this);
        pairRightSpinner_list.add(rightMemberSpinner);
        rightMemberSpinner.setAdapter(membersAdapter);
        rightMemberSpinner.setSelection(rightPosition);
        layout_horizontal_add.addView(rightMemberSpinner, addLp);
        Log.d("debug", "rightMemberSpinner追加　index：" + index);

        // 削除ボタン
        pairDeleteButton.setOnClickListener(v -> {
            Log.d("debug", "index：" + index);
            Log.d("debug", "fixedLinear.getChildCount()：" + fixedLinear.getChildCount());
            Log.d("debug", "pairLeftSpinner_list.size()：" + pairLeftSpinner_list.size());
            Log.d("debug", "pairRightSpinner_list.size()：" + pairRightSpinner_list.size());
            Log.d("debug", "pairLinearIndex_list.indexOf(index)：" + pairLinearIndex_list.indexOf(index));
            if (fixedLinear.getChildCount() > 1) {
                fixedLinear.removeViewAt(pairLinearIndex_list.indexOf(index));
                pairLeftSpinner_list.remove(pairLinearIndex_list.indexOf(index));
                pairRightSpinner_list.remove(pairLinearIndex_list.indexOf(index));
                pairLinearIndex_list.remove(pairLinearIndex_list.indexOf(index));
            }else {
                leftMemberSpinner.setSelection(0);
                rightMemberSpinner.setSelection(0);
            }
        });
        // メンバー左のスピナー--------------------------------
        leftMemberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }

            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                //選択されたアイテム名と位置（index)を内部変数へ保存
                String name = parent.getSelectedItem().toString();
                Log.d("debug", "name：" + name + "　position：" + position);
                Log.d("debug", "pairLinearIndex_list.indexOf(index)：" + pairLinearIndex_list.indexOf(index) + "　fixedLinear.getChildCount()-1：" + fixedLinear.getChildCount());
                if (position > 0 && pairLinearIndex_list.indexOf(index) == fixedLinear.getChildCount() - 1 && rightMemberSpinner.getSelectedItemPosition() > 0) {
                    // 追加
                    LinearLayout layout_horizontal_add = MakePairLinear();
                    fixedLinear.addView(layout_horizontal_add);
                    AddPairElement(fixedLinear.getChildCount() - 1, layout_horizontal_add, 0, 0);
                }
            }
        });

        // メンバー右のスピナー--------------------------------
        rightMemberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }

            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                //選択されたアイテム名と位置（index)を内部変数へ保存
                String name = parent.getSelectedItem().toString();
                Log.d("debug", "name：" + name + "　position：" + position);
                Log.d("debug", "pairLinearIndex_list.indexOf(index)：" + pairLinearIndex_list.indexOf(index) + "　fixedLinear.getChildCount()-1：" + fixedLinear.getChildCount());
                if (position > 0 && pairLinearIndex_list.indexOf(index) == fixedLinear.getChildCount() - 1 && leftMemberSpinner.getSelectedItemPosition() > 0) {
                    // 追加
                    LinearLayout layout_horizontal_add = MakePairLinear();
                    fixedLinear.addView(layout_horizontal_add);
                    AddPairElement(fixedLinear.getChildCount() - 1, layout_horizontal_add, 0, 0);
                }
            }
        });
    }
}
