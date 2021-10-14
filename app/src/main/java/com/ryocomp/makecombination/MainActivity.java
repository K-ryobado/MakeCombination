package com.ryocomp.makecombination;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MotionEventCompat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.text.InputFilter;
import android.text.InputType;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;

import static android.icu.text.DateTimePatternGenerator.PatternInfo.OK;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    List<EditText> memberEdit_list = new ArrayList<EditText>();
    List<String> memberName_list = new ArrayList<String>();
    List<String> memberSex_list = new ArrayList<String>();
    List<String> memberEnable_list = new ArrayList<String>();
    // Spinner
    List<Spinner> sexSpinner_list = new ArrayList<Spinner>();
    List<Spinner> enableSpinner_list = new ArrayList<Spinner>();
    // adaptar
    ArrayAdapter<String> sex_adapter;
    ArrayAdapter<String> enable_adapter;
    private TestOpenHelper helper;
    private SQLiteDatabase db;
    private String table="main";
    private String table2="setting";
    int memberMin = 4;
    int dbMaxRow;
    int inputMaxRow;
    String[] spinnerItems_sex;
    String[] spinnerItems_enable;
    String man_str = getResources().getString(R.string.man);
    String woman_str =getResources().getString(R.string.woman);
    String valid_str =getResources().getString(R.string.valid);
    String invalid_str =getResources().getString(R.string.invalid);
    // アルファベット配列
//    char []alphabet_big = new char[26];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerItems_sex = new String[]{"",man_str,woman_str};
        spinnerItems_enable = new String[]{valid_str,invalid_str};

        // 広告用の設定
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
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
            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLScnmS-IRFWNA6kNRrLiT9LIOBiHa7kY9LEp_7sDKjIMmtjcrw/viewform?usp=sf_link"));
            startActivity(intent);
        });

        // アルファベット配列を作成
//        char c = 'A';
//        for (int i = 0; i < 26; i++){
//            alphabet_big[i] = c++;
//        }

        // DB取得
        if(helper == null){
            helper = new TestOpenHelper(getApplicationContext());
        }

        if(db == null){
            db = helper.getWritableDatabase();
        }
        // テーブル削除
//        db.execSQL("drop table " +table+ " ;");
        //テーブル作成
//        db.execSQL("create table "+table +
//                    "(id integer primary key autoincrement," +
//                    "number integer not null," +
//                    "member string not null," +
//                    "sex string not null)");
//        db.execSQL("create table "+table2 +
//                    "(courtNumber string)");
        // テーブルリネーム
//        db.execSQL("alter table testdb rename to main;");
        // 列追加
//        db.execSQL("alter table "+table+" add column enable TEXT NOT NULL DEFAULT ''");
        // 列削除・リネーム（sqliteは対応していないためテーブルを再作成してデータを移す）
        //-- 既存のテーブルをリネーム
//        db.execSQL("ALTER TABLE "+table+" RENAME TO "+table+"_temp;");
//        //-- 新しいテーブルを作成（元々のテーブル名と同じ名前で）
//        db.execSQL("create table "+table +
//            "(id integer primary key autoincrement," +
//            "number integer," +
//            "member string," +
//            "sex string," +
//            "pair string," +
//            "rest string)");
        //-- レコードを全て移す
//        db.execSQL("INSERT INTO "+table+"(number,member,sex) SELECT number,member,sex FROM "+table+"_temp;");
        //-- 元のテーブルを削除
//        db.execSQL("DROP TABLE "+table+"_temp;");

        // adaptar作成
        sex_adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                spinnerItems_sex);
        enable_adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                spinnerItems_enable);

        //データ検索-------------------------------------------
        try {
            Log.d("debug","**********Cursor");
            Cursor cursor = db.query(
                    table,
                    new String[] { "number", "member","sex","enable" },
                    null,
                    null,
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();
            Log.d("debug","cursor.getCount()："+Integer.toString(cursor.getCount()));
            dbMaxRow = cursor.getCount();
            for (int i = 0; i < cursor.getCount(); i++) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(cursor.getInt(0));
                sbuilder.append(" ： "+cursor.getString(1));
                memberName_list.add(cursor.getString(1));
                memberSex_list.add(cursor.getString(2));
                memberEnable_list.add(cursor.getString(3));
                Log.d("debug","**********"+sbuilder.toString());

                cursor.moveToNext();
            }
            // 忘れずに！
            cursor.close();
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }//------------------------------------------------------

        // スクロールビュー内のリニアレイアウト
        LinearLayout childLayout = findViewById(R.id.linear_members);
        // 追加ボタン
        TextView addText = findViewById(R.id.plusButton);

        //追加
        addText.setOnClickListener(v -> {
            int memberCount = childLayout.getChildCount();
            // リニアレイアウトのインスタンス生成
            LinearLayout layout_horizontal = MakeMemberLinear(this);
            // 追加
            childLayout.addView(layout_horizontal,memberCount-1);
            // Linearに追加
            AddMemberElement(layout_horizontal,memberCount,"",0,0);
            Toast.makeText(this,memberCount+" "+R.string.add,Toast.LENGTH_SHORT).show();
        });

        // 削除ボタン
        TextView removeText = findViewById(R.id.minusButton);

        //削除
        removeText.setOnClickListener(v -> {
            int memberCount = childLayout.getChildCount();
            if (memberCount >  memberMin + 1){
                childLayout.removeViewAt(memberCount-2);
                memberEdit_list.remove(memberEdit_list.size()-1);
            }
            Toast.makeText(this,memberCount-1+" "+R.string.delete,Toast.LENGTH_SHORT).show();
        });
        //----------------------------------------------------------------

        // メンバー一覧-----------------------------------------------------
        int memberDefault = memberMin;
        if (memberName_list.size() > memberDefault){
            memberDefault = memberName_list.size();
        }
        for (int i=0 ; i < memberDefault ;i++){
            // リニアレイアウトのインスタンス生成
            LinearLayout layout_horizontal = MakeMemberLinear(this);
            // 追加
            childLayout.addView(layout_horizontal);

            // 登録済みのメンバー名
            String memberName = "";
            if (memberName_list.size() >= i+1){
//                editMember.setText(memberName_list.get(i));
                memberName = memberName_list.get(i);
            }

            // 登録済みの性別の位置
            int sex_position=0;
            if (i < memberSex_list.size()){
//                Log.d("debug","memberSex_list.size()："+Integer.toString(memberSex_list.size()));
                sex_position = sex_adapter.getPosition(memberSex_list.get(i));
//                spinner.setSelection(spinnerPosition);
            }
            Log.d("debug","spinnerPosition："+Integer.toString(sex_position));

            //登録済みの有効無効の位置
            int enable_position = 0;
            if (i < memberEnable_list.size()){
                enable_position = enable_adapter.getPosition(memberEnable_list.get(i));
            }
            // linearに追加
            AddMemberElement(layout_horizontal,i+1,memberName,sex_position,enable_position);
        }//-----------------------------------------------------------------

        // 登録ボタン--------------------------------------------------------
        LinearLayout layout_horizontal = MakeMemberLinear(this);
        //追加
        childLayout.addView(layout_horizontal);

        Button combinationSettingButton = findViewById(R.id.settingButton);
        // 登録処理
        combinationSettingButton.setOnClickListener(v -> {
            int maxStringIndex = -1;
            for (int i=memberEdit_list.size()-1; i >= 0; i--){
                String editValue  = memberEdit_list.get(i).getText().toString().trim();
                if (editValue.length() != 0){
                    maxStringIndex = i;
                    Log.d("debug", "maxStringIndex："+Integer.toString(maxStringIndex));
                    break;
                }
            }

            //4人以上のデータがなかったらアラートを出す
            if(maxStringIndex < 3){
                new AlertDialog.Builder( this )
                        .setTitle(R.string.alert)
                        .setMessage( R.string.msg1 )
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK" , null )
                        .show();
                return;
            }

            // 現在のテーブルデータを削除
//            db.delete(table,null,null);

            ArrayList<String> regMembers = new ArrayList<String>();
            for (int i=0; i < memberEdit_list.size(); i++){
                if ( maxStringIndex >= i){
                    String editValue  = memberEdit_list.get(i).getText().toString().trim();
                    String sexValue = sexSpinner_list.get(i).getSelectedItem().toString();
                    String enableValue = enableSpinner_list.get(i).getSelectedItem().toString();
                    Log.d("debug", "index："+Integer.toString(i)+" , editValue："+editValue);
                    if (editValue.length() != 0){
                        // 同じ名前が来たら弾く
                        if (regMembers.contains(editValue)){
                            new AlertDialog.Builder( this )
                                    .setTitle( R.string.alert )
                                    .setMessage( "No."+i+1+" "+R.string.msg2 )
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("OK" , null )
                                    .show();
                            return;
                        }
                        regMembers.add(editValue);
                    }else {
                        editValue = R.string.anonymous+Integer.toString(i+1);
                        memberEdit_list.get(i).setText(R.string.anonymous+(i+1));
                    }
                    if (dbMaxRow > i){
                        UpdateData(db,table,editValue,sexValue,enableValue,"_id = "+Integer.toString(i+1));
                    }else {
                        InsertData(db, table,i+1, editValue,sexValue,enableValue);
                    }
                    inputMaxRow++;
                }
            }
            // データ削除
            if (inputMaxRow < dbMaxRow){
                for (int i=inputMaxRow; i<dbMaxRow; i++){
                    db.delete(table,"_id = "+Integer.toString(i+1),null);
                }
            }

            //アクティビティ遷移
            Intent intent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
        });//----------------------------------------------------------------
    }

    private void InsertData(SQLiteDatabase db,String table, int index, String value,String sex,String enable){

        ContentValues values = new ContentValues();
        values.put("number", index);
        values.put("member", value);
        values.put("sex", sex);
        values.put("pair", "");
        values.put("rest", "");
        values.put("enable", enable);

        db.insert(table, null, values);
    }

    private void UpdateData(SQLiteDatabase db,String table, String value ,String sex,String enable,String where){

        ContentValues values = new ContentValues();
        values.put("member", value);
        values.put("sex", sex);
        values.put("enable", enable);

        db.update(table, values,where,null);
    }

    //dpをpxに置換
    public static int convertDpToPx(Context context, int dp) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * d) + 0.5);
    }

    private LinearLayout MakeMemberLinear(Context context){
        // リニアレイアウトのインスタンス生成
        LinearLayout layout_horizontal = new LinearLayout(context);
        layout_horizontal.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(convertDpToPx(this,5),convertDpToPx(this,3),0,0);
        layout_horizontal.setLayoutParams(lp);

        layout_horizontal.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        return layout_horizontal;
    }

    private void AddMemberElement(LinearLayout layout_horizontal, int memberNumber, String memberName, int sexPosition, int enablePosition){
        // 番号
        TextView text = new TextView(this);
        text.setTextSize(15);
        String memberNumber_str = Integer.toString(memberNumber);
        if (memberNumber_str.length() == 1){
            memberNumber_str = "0"+memberNumber_str;
        }
        text.setText(" "+memberNumber_str);
        // linearに追加
        layout_horizontal.addView(
                text,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // 入力欄
        EditText editMember = new EditText(this);
        editMember.setText(memberName);
        editMember.setTextSize(15);
        editMember.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        editMember.setWidth(convertDpToPx(this,165));
        editMember.setInputType(InputType.TYPE_CLASS_TEXT);
        memberEdit_list.add(editMember);
        // linearに追加
        layout_horizontal.addView(editMember);

        // 性別のスピナー--------------------------------
        Spinner sexSpinner = new Spinner(this);
        sex_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sexSpinner.setAdapter(sex_adapter);
        sexSpinner_list.add(sexSpinner);
        //DBの値を設定
        sexSpinner.setSelection(sexPosition);
        // linearに追加
        layout_horizontal.addView(sexSpinner);
        //選択時の動作
        sexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }

            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                //選択されたアイテム名と位置（index)を内部変数へ保存
                String string =  parent.getSelectedItem().toString();
                int index = position;
                if (string == man_str){
                    layout_horizontal.setBackgroundColor(Color.parseColor("#D9E5FF"));
                }else if(string == woman_str){
                    layout_horizontal.setBackgroundColor(Color.parseColor("#FFD5EC"));
                }else {
                    layout_horizontal.setBackgroundColor(Color.WHITE);
                }
            }
        });
        //-------------------------------------------------
        // 有効・無効のスピナー--------------------------------
        Spinner enable_spinner = new Spinner(this);
        enable_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        enable_spinner.setAdapter(enable_adapter);
        enableSpinner_list.add(enable_spinner);
        //DBの値を設定
        enable_spinner.setSelection(enablePosition);
        // linearに追加
        layout_horizontal.addView(enable_spinner);
        //選択時の動作
        enable_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }

            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                //選択されたアイテム名と位置（index)を内部変数へ保存
                String string =  parent.getSelectedItem().toString();
                int index = position;
                if (string == String.valueOf(R.string.invalid)){
                    layout_horizontal.setBackgroundColor(Color.GRAY);
                    editMember.setEnabled(false);
                    sexSpinner.setEnabled(false);
                }else if(string == String.valueOf(R.string.valid)){
                    if (sexSpinner.getSelectedItem().toString().equals(R.string.man)){
                        layout_horizontal.setBackgroundColor(Color.parseColor("#D9E5FF"));
                    }else if(sexSpinner.getSelectedItem().toString().equals(R.string.woman)){
                        layout_horizontal.setBackgroundColor(Color.parseColor("#FFD5EC"));
                    }else {
                        layout_horizontal.setBackgroundColor(Color.WHITE);
                    }
                    editMember.setEnabled(true);
                    sexSpinner.setEnabled(true);
                }
            }
        });
        //-------------------------------------------------
    }
}