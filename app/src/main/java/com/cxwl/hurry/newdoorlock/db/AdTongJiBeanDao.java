package com.cxwl.hurry.newdoorlock.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "AD_TONG_JI_BEAN".
*/
public class AdTongJiBeanDao extends AbstractDao<AdTongJiBean, Long> {

    public static final String TABLENAME = "AD_TONG_JI_BEAN";

    /**
     * Properties of entity AdTongJiBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Add_id = new Property(1, int.class, "add_id", false, "add_id");
        public final static Property Start_time = new Property(2, String.class, "start_time", false, "start_time");
        public final static Property End_time = new Property(3, String.class, "end_time", false, "end_time");
        public final static Property Mac = new Property(4, String.class, "mac", false, "mac");
    }


    public AdTongJiBeanDao(DaoConfig config) {
        super(config);
    }
    
    public AdTongJiBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"AD_TONG_JI_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"add_id\" INTEGER NOT NULL ," + // 1: add_id
                "\"start_time\" TEXT," + // 2: start_time
                "\"end_time\" TEXT," + // 3: end_time
                "\"mac\" TEXT);"); // 4: mac
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"AD_TONG_JI_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, AdTongJiBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getAdd_id());
 
        String start_time = entity.getStart_time();
        if (start_time != null) {
            stmt.bindString(3, start_time);
        }
 
        String end_time = entity.getEnd_time();
        if (end_time != null) {
            stmt.bindString(4, end_time);
        }
 
        String mac = entity.getMac();
        if (mac != null) {
            stmt.bindString(5, mac);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, AdTongJiBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getAdd_id());
 
        String start_time = entity.getStart_time();
        if (start_time != null) {
            stmt.bindString(3, start_time);
        }
 
        String end_time = entity.getEnd_time();
        if (end_time != null) {
            stmt.bindString(4, end_time);
        }
 
        String mac = entity.getMac();
        if (mac != null) {
            stmt.bindString(5, mac);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public AdTongJiBean readEntity(Cursor cursor, int offset) {
        AdTongJiBean entity = new AdTongJiBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // add_id
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // start_time
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // end_time
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // mac
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, AdTongJiBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAdd_id(cursor.getInt(offset + 1));
        entity.setStart_time(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setEnd_time(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setMac(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(AdTongJiBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(AdTongJiBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(AdTongJiBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
