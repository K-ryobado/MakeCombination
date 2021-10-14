package com.ryocomp.makecombination;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.LinearGradient;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MakeCombActivity extends AppCompatActivity {
    private List<String> db_member_list = new ArrayList<String>();
    private List<String> db_sex_list = new ArrayList<String>();
    private List<String> db_pair_list = new ArrayList<String>();
    private List<String> db_rest_list = new ArrayList<String>();

    private TestOpenHelper helper;
    private SQLiteDatabase db;
    private String table = "main";
    private String table2 = "setting";
    private Boolean mixOnly = false;
    private String courtNumber_db;
    private String mixOnly_db;
    private Boolean restChecked;
    private Boolean pairChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make);

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

        //intentの取得
        Intent intent_setting = getIntent();
        restChecked = intent_setting.getBooleanExtra("restChecked", false);
        pairChecked = intent_setting.getBooleanExtra("pairChecked", false);
        Log.d("debug", "restChecked：" + restChecked + "　pairChecked：" + pairChecked);

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

//            db_member_list.add("");
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.getString(5).equals("有効")) {
                    StringBuilder sbuilder = new StringBuilder();
                    sbuilder.append(cursor.getInt(0) + " ： " + cursor.getString(1) + " ： " + cursor.getString(2) + " ： " + cursor.getString(3) + " ： " + cursor.getString(4));
                    db_member_list.add(cursor.getString(1));
                    db_sex_list.add(cursor.getString(2));
                    db_pair_list.add(cursor.getString(3));
                    db_rest_list.add(cursor.getString(4));
                    Log.d("debug", "**********" + sbuilder.toString());
                }
                cursor.moveToNext();
            }
            // 忘れずに！
            cursor.close();
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }//------------------------------------------------------

        // 大元のLinearLayout
        LinearLayout mainLayout = findViewById(R.id.list);

        // 整数の乱数を重複なしで生成
//        ArrayList<Integer> intList = new ArrayList<Integer>();
//        int index = candidateMembers.size();
//        Log.d("debug","index："+index);
//        for (int i=0; i<index; i++){
//            intList.add(i);
//        }
        // シャッフル
//        Collections.shuffle(intList);
//        Log.d("debug","intList："+intList.size());        

        // 固定ペア設定
        ArrayList<String> fixedPairAry = new ArrayList<>();
        if (pairChecked) {
            for (int i = 0; i < db_pair_list.size(); i++) {
                String pair = db_pair_list.get(i);
                String leftName = db_member_list.get(i);
                String rightName = "";
                Log.d("debug", "pair：" + pair);
                Log.d("debug", "leftName：" + leftName);
                if (!pair.equals("")) {
                    String pairNumber = pair.substring(0, pair.length() - 1);
                    Log.d("debug", "pairNumber：" + pairNumber);
                    if (pair.indexOf("L") > -1) {
                        int rightIndex = 0;
                        for (int p = 0; p < db_pair_list.size(); p++) {
                            Log.d("debug", "db_pair_list.get(p)：" + db_pair_list.get(p));
                            Log.d("debug", "db_pair_list.get(p).equals(pairNumber+\"R\")：" + db_pair_list.get(p).equals(pairNumber + "R"));
                            if (db_pair_list.get(p).equals(pairNumber + "R")) {
                                rightIndex = p;
                                break;
                            }
                        }
                        Log.d("debug", "rightIndex：" + rightIndex);
                        if (rightIndex > -1) {
                            rightName = db_member_list.get(rightIndex);
                            fixedPairAry.add(leftName + "@@" + rightName);
                        }
                    }
                }
            }
        }

        // 休憩・試合設定  
        ArrayList<String> gameMembers = new ArrayList<String>();
        ArrayList<String> restMembers = new ArrayList<String>();
        ArrayList<String> noRestMembers = new ArrayList<String>();
        ArrayList<String> candidateMembers = new ArrayList<String>();
        ArrayList<String> combinationAry = new ArrayList<>();
        ArrayList<String> pairAry = new ArrayList<>();
        ArrayList<String> gamePairAry = new ArrayList<>();
        if (restChecked) {
            for (int i = 0; i < db_rest_list.size(); i++) {
                String str = db_member_list.get(i);
                Log.d("debug", "str：" + str);
                if (db_rest_list.get(i).equals("試合")) {
                    // 試合メンバー
                    gameMembers.add(str);
                    noRestMembers.add(str);
                } else if (db_rest_list.get(i).equals("休憩")) {
                    restMembers.add(str);
                } else {
                    // 試合候補メンバー
                    candidateMembers.add(str);
                    noRestMembers.add(str);
                }
            }
            Log.d("debug", "candidateMembers：" + candidateMembers.size());

            //gameMembers から固定ペアがいればぬく
            int fixedMember = 0;
            for (String str : fixedPairAry) {
                String[] pair = str.split("@@");
                int leftIndex = gameMembers.indexOf(pair[0]);
                Boolean gamePairCheck = false;
                if (leftIndex > -1) {
                    fixedMember++;
                    gameMembers.remove(leftIndex);
                    gamePairCheck = true;
                }
                int rightIndex = gameMembers.indexOf(pair[1]);
                if (rightIndex > -1) {
                    fixedMember++;
                    gameMembers.remove(rightIndex);
                    gamePairCheck = true;
                }
                if (gamePairCheck) {
                    // 試合確定のペアへ追加
                    gamePairAry.add(str);
                }
                leftIndex = candidateMembers.indexOf(pair[0]);
                if (leftIndex > -1) {
                    fixedMember++;
                    candidateMembers.remove(leftIndex);
                }
                rightIndex = candidateMembers.indexOf(pair[1]);
                if (rightIndex > -1) {
                    fixedMember++;
                    candidateMembers.remove(rightIndex);
                }
            }

            // 試合優先メンバーをシャッフル
            Collections.shuffle(gameMembers);
            // 候補メンバーに優先メンバー(優先固定ペアを除いた)を追加してシャッフル
            candidateMembers.addAll(gameMembers);
            Collections.shuffle(candidateMembers);

            // 先に試合優先メンバーを追加
            for (String m : gameMembers) {
                if (combinationAry.contains(m)) {
                    continue;
                }
                for (String s : candidateMembers) {
                    if (combinationAry.contains(s)) {
                        continue;
                    }
                    combinationAry.add(m);
                    combinationAry.add(s);
                }
            }
            // 候補メンバーを追加
            for (String s : candidateMembers) {
                if (combinationAry.contains(s)) {
                    continue;
                }
                combinationAry.add(s);
            }

            // ペア一覧を作成（優先メンバーを先頭にする）
            ArrayList<String> candidatePairAry = new ArrayList<>();
            for (int p = 0; p < combinationAry.size(); p = p + 2) {
                if (p + 1 < combinationAry.size()) {
                    // 優先メンバー以外を追加
                    if (!gameMembers.contains(combinationAry.get(p)) && !gameMembers.contains(combinationAry.get(p+1))){
                        candidatePairAry.add(combinationAry.get(p) + "@@" + combinationAry.get(p + 1));
                    }
                }
            }
            // 優先じゃない固定ペアを追加
            for (String a : fixedPairAry){
                if (!gamePairAry.contains(a)){
                    candidatePairAry.add(a);
                }
            }
            Collections.shuffle(candidatePairAry);
            // 優先メンバーを追加
            for (int p = 0; p < combinationAry.size(); p = p + 2) {
                if (p + 1 < combinationAry.size()) {
                    // 優先メンバーを追加
                    if (gameMembers.contains(combinationAry.get(p)) || gameMembers.contains(combinationAry.get(p+1))){
                        candidatePairAry.add(combinationAry.get(p) + "@@" + combinationAry.get(p + 1));
                    }
                }
            }
            //優先メンバ―が先頭にくるようにする
            Collections.reverse(candidatePairAry);
//
            // 組み合わせ数を取得
            int number = noRestMembers.size() / 4;
            Log.d("debug", "number：" + number);
            int loopMax;
            if (Integer.parseInt(courtNumber_db) < number) {
                loopMax = Integer.parseInt(courtNumber_db);
            } else {
                loopMax = number;
            }

            pairAry.addAll(gamePairAry);
            for (int a = 0; pairAry.size() < loopMax * 2; a++) {
                pairAry.add(candidatePairAry.get(a));
            }
        } else {
            combinationAry = new ArrayList<>(db_member_list);


            int number = combinationAry.size() / 4;
            Log.d("debug", "number：" + number);
            int loopMax;
            if (Integer.parseInt(courtNumber_db) < number) {
                loopMax = Integer.parseInt(courtNumber_db);
            } else {
                loopMax = number;
            }

            //combinationAry から固定ペアがいればぬく
            int fixedMember = 0;
            for (String str : fixedPairAry) {
                String[] pair = str.split("@@");
                Log.d("debug", "pair[0]：" + pair[0] + "　pair[1]：" + pair[1]);
                int leftIndex = combinationAry.indexOf(pair[0]);
                Log.d("debug", "leftIndex：" + leftIndex);
                if (leftIndex > -1) {
                    fixedMember++;
                    combinationAry.remove(leftIndex);
                }
                int rightIndex = combinationAry.indexOf(pair[1]);
                Log.d("debug", "rightIndex：" + rightIndex);
                if (rightIndex > -1) {
                    fixedMember++;
                    combinationAry.remove(rightIndex);
                }
            }

            // 固定ペアを除いたメンバーをシャッフル
            Collections.shuffle(combinationAry);
            // 候補ペア一覧を作成
            ArrayList<String> candidatePairAry = new ArrayList<>(fixedPairAry);
            for (int a = 0; a < combinationAry.size(); a = a + 2) {
                if (a + 1 < combinationAry.size()) {
                    candidatePairAry.add(combinationAry.get(a) + "@@" + combinationAry.get(a + 1));
                }
            }
            // 候補ペアをシャッフル
            Collections.shuffle(candidatePairAry);
            // 試合するペアを決定
            for (int a = 0; a < loopMax * 2; a++) {
                pairAry.add(candidatePairAry.get(a));
            }
        }

        Collections.shuffle(pairAry);

        Log.d("debug", "pairAry.size()：" + pairAry.size());
        for (int f = 0; f < pairAry.size(); f = f + 2) {
            String[] pairAry1 = pairAry.get(f).split("@@");
            String[] pairAry2 = pairAry.get(f + 1).split("@@");
            String member1 = pairAry1[0];
            String member2 = pairAry1[1];
            String member3 = pairAry2[0];
            String member4 = pairAry2[1];
            Log.d("debug", "pairAry1[0]：" + member1);
            Log.d("debug", "pairAry1[1]：" + member2);
            Log.d("debug", "pairAry2[0]：" + member3);
            Log.d("debug", "pairAry2[1]：" + member4);

            // レイアウト生成----------------------------------

            // Relative Layout を使ってベースを作ってそれを流用する
            RelativeLayout relativeLayout = new RelativeLayout(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            relativeLayout.setLayoutParams(params);
            relativeLayout.setBackgroundResource(R.drawable.border);
            mainLayout.addView(relativeLayout);


            // 組み合わせ
            TextView pair1 = new TextView(this);
//            pair1.setLayoutParams(addLp);
            pair1.setText(member1 + "\n" + member2);
            pair1.setTypeface(Typeface.DEFAULT_BOLD);
            pair1.setTextSize(16);
//            layout_horizontal_add.addView(pair1, addLp);
            TextView vs = new TextView(this);
            vs.setGravity(Gravity.BOTTOM);
//            vs.setLayoutParams(addLp);
            vs.setText("　VS　");
            vs.setTypeface(Typeface.DEFAULT_BOLD);
//            layout_horizontal_add.addView(vs, addLp);
            TextView pair2 = new TextView(this);
//            pair2.setLayoutParams(addLp);
            pair2.setText(member3 + "\n" + member4);
            pair2.setTypeface(Typeface.DEFAULT_BOLD);
            pair2.setTextSize(16);
//            layout_horizontal_add.addView(pair2, addLp);

            // vs をセンターに配置
            RelativeLayout.LayoutParams add_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            add_lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            vs.setId(f + 1);
            relativeLayout.addView(vs, add_lp);
            // 左に配置
            RelativeLayout.LayoutParams add_lp_left = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            add_lp_left.addRule(RelativeLayout.RIGHT_OF, f + 1);
            add_lp_left.addRule(RelativeLayout.ALIGN_LEFT);
            add_lp_left.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayout.addView(pair1, add_lp_left);
            // 右に配置
            RelativeLayout.LayoutParams add_lp_right = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            add_lp_right.addRule(RelativeLayout.LEFT_OF, f + 1);
            add_lp_right.addRule(RelativeLayout.ALIGN_RIGHT);
            add_lp_right.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayout.addView(pair2, add_lp_right);
        }

        //メンバー設定画面へ******************************************************************
        Button memberButton = findViewById(R.id.memberButton);
        memberButton.setOnClickListener(v ->
        {
            // 画面遷移
            Intent intent = new Intent(MakeCombActivity.this, MainActivity.class);
            startActivity(intent);
        });//********************************************************************************

        //組み合わせ設定画面へ****************************************************************
        Button combinationArySettingButton = findViewById(R.id.settingButton);
        combinationArySettingButton.setOnClickListener(v ->
        {
            // 画面遷移
            Intent intent = new Intent(MakeCombActivity.this, SettingActivity.class);
            startActivity(intent);
        });//********************************************************************************
    }

    //dpをpxに置換
    public static int convertDpToPx(Context context, int dp) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * d) + 0.5);
    }
}
