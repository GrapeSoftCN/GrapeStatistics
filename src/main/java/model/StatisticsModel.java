package model;

import apps.appsProxy;
import database.DBHelper;
import database.db;
import nlogger.nlogger;

public class StatisticsModel {
	private String appid = appsProxy.appidString();

	private db getdb(String tableName) {
		DBHelper helper = new DBHelper(appsProxy.configValue().getString("db"), tableName);
		return helper;
	}

	public db bind(String tableName) {
		return getdb(tableName).bind(appid);
	}
}
