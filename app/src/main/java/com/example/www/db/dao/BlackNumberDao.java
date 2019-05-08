package com.example.www.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.www.db.BlackNumberOpenHelper;
import com.example.www.db.domain.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;

public class BlackNumberDao {

    private final BlackNumberOpenHelper mBlackNumberOpenHelper;

    //blacknumberdao单例模式
    //1、私有化构造方法
    private BlackNumberDao (Context context){
        //创建数据库以及其表结构
        mBlackNumberOpenHelper = new BlackNumberOpenHelper(context);
    }
    // 2、声明一个当前类对象
    private static BlackNumberDao mBlackNumberDao = null;
    // 3、 提供一个静态方法，如果当前类对象为空 就创建一个新的
    public static BlackNumberDao getInstance(Context context){
        if(mBlackNumberDao == null) {
            mBlackNumberDao = new BlackNumberDao(context);
        }

        return mBlackNumberDao;
    }


    /***
     * 增加一个条目
     * @param phone 拦截的电话号码
     * @param mode 拦截的类型 （1 短信 2 电话 3 拦截所有（短信 + 电话））
     */
    public void insert(String phone, String mode){
        SQLiteDatabase writableDatabase = mBlackNumberOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        contentValues.put("mode", mode);
        writableDatabase.insert("blacknumber", null, contentValues);

        writableDatabase.close();
    }

    /***
     * 删除一条数据
     * @param phone 根据电话号码删除
     */
    public void delete(String phone){
        SQLiteDatabase writableDatabase = mBlackNumberOpenHelper.getWritableDatabase();
        writableDatabase.delete("blacknumber", "phone = ?", new String[]{phone});

        writableDatabase.close();
    }

    /***
     * 修改一条数据
     * @param phone 根据电话修改
     * @param mode 要更新的数据
     */
    public void update(String phone, String mode){
        SQLiteDatabase writableDatabase = mBlackNumberOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("mode", mode);
        writableDatabase.update("blacknumber", contentValues, "phone = ?", new String[]{phone});

        writableDatabase.close();
    }

    public List<BlackNumberInfo> findAll(){
        SQLiteDatabase writableDatabase = mBlackNumberOpenHelper.getWritableDatabase();
        Cursor cursor = writableDatabase.query("blacknumber", new String[]{"phone", "mode"}, null, null, null, null, "_id desc");// 根据 ）——id字段 倒序排
        List<BlackNumberInfo> blackInfoList = new ArrayList<>();
        while (cursor.moveToNext()){
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            String phone = cursor.getString(0);
            String mode = cursor.getString(1);
            blackNumberInfo.setMode(mode);
            blackNumberInfo.setPhone(phone);

            blackInfoList.add(blackNumberInfo);
        }
        cursor.close();
        writableDatabase.close();
        return blackInfoList;
    }
}