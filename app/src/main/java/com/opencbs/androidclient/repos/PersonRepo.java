package com.opencbs.androidclient.repos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.opencbs.androidclient.models.Address;
import com.opencbs.androidclient.models.CustomFieldHeader;
import com.opencbs.androidclient.models.CustomValue;
import com.opencbs.androidclient.models.Person;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PersonRepo {

    @Inject
    DbHelper dbHelper;

    public void deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from people");
        db.execSQL(
                "delete from custom_field_values " +
                        "where owner_id in (select uuid from people)"
        );
    }

    public void add(Person person) {
        ArrayList<Person> people = new ArrayList<>();
        people.add(person);
        add(people);
    }

    public void add(List<Person> people) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Person person : people) {
            try {
                db.beginTransaction();
                ContentValues contentValues = new ContentValues();
                contentValues.put("id", person.id);
                contentValues.put("uuid", person.uuid);
                contentValues.put("first_name", person.firstName);
                contentValues.put("last_name", person.lastName);
                contentValues.put("father_name", person.fatherName);
                contentValues.put("sex", person.sex);
                contentValues.put("birth_date", dateFormat.format(person.birthDate));
                contentValues.put("birth_place", person.birthPlace);
                contentValues.put("identification_data", person.identificationData);
                contentValues.put("activity_id", person.activityId);
                contentValues.put("branch_id", person.branchId);
                contentValues.put("personal_phone", person.personalPhone);
                contentValues.put("home_phone", person.homePhone);
                contentValues.put("email", person.email);
                contentValues.put("city_1_id", person.address1.cityId);
                contentValues.put("address_1", person.address1.address);
                contentValues.put("postal_code_1", person.address1.postalCode);
                contentValues.put("city_2_id", person.address2.cityId);
                contentValues.put("address_2", person.address2.address);
                contentValues.put("postal_code_2", person.address2.postalCode);
                db.insert("people", null, contentValues);

                if (person.customInformation != null) {
                    for (CustomValue customValue : person.customInformation) {
                        ContentValues customContentValues = new ContentValues();
                        customContentValues.put("field_id", customValue.field.id);
                        customContentValues.put("owner_id", person.uuid);
                        customContentValues.put("value", customValue.value);
                        db.insert("custom_field_values", null, customContentValues);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public Person get(String uuid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Person person = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from people where uuid = ?", new String[]{uuid});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                person = new Person();
                person.address1 = new Address();
                person.address2 = new Address();
                person.id = cursor.getInt(cursor.getColumnIndex("id"));
                person.uuid = cursor.getString(cursor.getColumnIndex("uuid"));
                person.firstName = cursor.getString(cursor.getColumnIndex("first_name"));
                person.lastName = cursor.getString(cursor.getColumnIndex("last_name"));
                person.fatherName = cursor.getString(cursor.getColumnIndex("father_name"));
                person.sex = cursor.getString(cursor.getColumnIndex("sex"));
                person.birthDate = dateFormat.parse(cursor.getString(cursor.getColumnIndex("birth_date")));
                person.birthPlace = cursor.getString(cursor.getColumnIndex("birth_place"));
                person.identificationData = cursor.getString(cursor.getColumnIndex("identification_data"));
                person.activityId = cursor.getInt(cursor.getColumnIndex("activity_id"));
                person.branchId = cursor.getInt(cursor.getColumnIndex("branch_id"));
                person.personalPhone = cursor.getString(cursor.getColumnIndex("personal_phone"));
                person.homePhone = cursor.getString(cursor.getColumnIndex("home_phone"));
                person.email = cursor.getString(cursor.getColumnIndex("email"));
                person.address1.cityId = cursor.getInt(cursor.getColumnIndex("city_1_id"));
                person.address1.address = cursor.getString(cursor.getColumnIndex("address_1"));
                person.address1.postalCode = cursor.getString(cursor.getColumnIndex("postal_code_1"));
                person.address2.cityId = cursor.getInt(cursor.getColumnIndex("city_2_id"));
                person.address2.address = cursor.getString(cursor.getColumnIndex("address_2"));
                person.address2.postalCode = cursor.getString(cursor.getColumnIndex("postal_code_2"));
            }
        } catch (ParseException ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (person != null) {
            person.customInformation = new ArrayList<>();
            try {
                cursor = db.rawQuery(
                        "select field_id, value from custom_field_values where owner_id = ?",
                        new String[]{person.uuid});
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        CustomValue customValue = new CustomValue();
                        customValue.field = new CustomFieldHeader();
                        customValue.field.id = cursor.getInt(cursor.getColumnIndex("field_id"));
                        customValue.value = cursor.getString(cursor.getColumnIndex("value"));
                        person.customInformation.add(customValue);
                        cursor.moveToNext();
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return person;
    }

    public void sync(Person person) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(
                "update people set id = ? where uuid = ?",
                new String[]{person.id + "", person.uuid}
        );
    }
}
