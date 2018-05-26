package com.cxwl.hurry.newdoorlock.db;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.cxwl.hurry.newdoorlock.db.AdTongJiBean;
import com.cxwl.hurry.newdoorlock.db.Ka;
import com.cxwl.hurry.newdoorlock.db.Lian;
import com.cxwl.hurry.newdoorlock.db.LogDoor;

import com.cxwl.hurry.newdoorlock.db.AdTongJiBeanDao;
import com.cxwl.hurry.newdoorlock.db.KaDao;
import com.cxwl.hurry.newdoorlock.db.LianDao;
import com.cxwl.hurry.newdoorlock.db.LogDoorDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig adTongJiBeanDaoConfig;
    private final DaoConfig kaDaoConfig;
    private final DaoConfig lianDaoConfig;
    private final DaoConfig logDoorDaoConfig;

    private final AdTongJiBeanDao adTongJiBeanDao;
    private final KaDao kaDao;
    private final LianDao lianDao;
    private final LogDoorDao logDoorDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        adTongJiBeanDaoConfig = daoConfigMap.get(AdTongJiBeanDao.class).clone();
        adTongJiBeanDaoConfig.initIdentityScope(type);

        kaDaoConfig = daoConfigMap.get(KaDao.class).clone();
        kaDaoConfig.initIdentityScope(type);

        lianDaoConfig = daoConfigMap.get(LianDao.class).clone();
        lianDaoConfig.initIdentityScope(type);

        logDoorDaoConfig = daoConfigMap.get(LogDoorDao.class).clone();
        logDoorDaoConfig.initIdentityScope(type);

        adTongJiBeanDao = new AdTongJiBeanDao(adTongJiBeanDaoConfig, this);
        kaDao = new KaDao(kaDaoConfig, this);
        lianDao = new LianDao(lianDaoConfig, this);
        logDoorDao = new LogDoorDao(logDoorDaoConfig, this);

        registerDao(AdTongJiBean.class, adTongJiBeanDao);
        registerDao(Ka.class, kaDao);
        registerDao(Lian.class, lianDao);
        registerDao(LogDoor.class, logDoorDao);
    }
    
    public void clear() {
        adTongJiBeanDaoConfig.clearIdentityScope();
        kaDaoConfig.clearIdentityScope();
        lianDaoConfig.clearIdentityScope();
        logDoorDaoConfig.clearIdentityScope();
    }

    public AdTongJiBeanDao getAdTongJiBeanDao() {
        return adTongJiBeanDao;
    }

    public KaDao getKaDao() {
        return kaDao;
    }

    public LianDao getLianDao() {
        return lianDao;
    }

    public LogDoorDao getLogDoorDao() {
        return logDoorDao;
    }

}
