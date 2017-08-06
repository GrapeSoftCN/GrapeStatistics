package interfaceApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import database.DBHelper;
import nlogger.nlogger;
import security.codec;

public class Statistics {
	private String DBName;
	private JSONObject authDB;

	public Statistics() {
		DBName = "mongodb";
		authDB = new JSONObject();
	}
	public String insert(String authCode, String tableName, String data) {
		int rb = 0;
		DBHelper db = new DBHelper(DBName, tableName);
		JSONObject inputData = JSONObject.toJSON(data);
		try {
			if (inputData != null) {
				db.data(inputData).insert();
				rb = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			rb = 0;
		}
		return jsonShow(authCode, rb);
	}

	public String findOne(String authCode, String tableName, String data) {
		DBHelper db = new DBHelper(DBName, tableName);
		JSONObject rs = db.where(JSONArray.toJSONArray(data)).find();
		if (rs == null) {
			rs = new JSONObject();
		}
		return jsonShow(authCode, rs);
	}

	public String find(String authCode, String tableName, String data, int max) {
		DBHelper db = new DBHelper(DBName, tableName);
		if (max > 0) {
			db.limit(max);
		}
		return arrayShow(authCode, db.where(JSONArray.toJSONArray(data)).select());
	}

	public String updateOne(String authCode, String tableName, String data, String updateData) {
		DBHelper db = new DBHelper(DBName, tableName);
		return jsonShow(authCode, db.where(JSONArray.toJSONArray(data)).data(updateData).update());
	}

	public String update(String authCode, String tableName, String data, String updateData, int max) {
		DBHelper db = new DBHelper(DBName, tableName);
		if (max > 0) {
			db.limit(max);
		}
		return jsonShow(authCode, db.where(JSONArray.toJSONArray(data)).data(updateData).updateAll());
	}

	public String deleteOne(String authCode, String tableName, String data) {
		DBHelper db = new DBHelper(DBName, tableName);
		return jsonShow(authCode, db.where(JSONArray.toJSONArray(data)).delete());
	}

	public String delete(String authCode, String tableName, String data, int max) {
		DBHelper db = new DBHelper(DBName, tableName);
		if (max > 0) {
			db.limit(max);
		}
		return jsonShow(authCode, db.where(JSONArray.toJSONArray(data)).deleteAll());
	}
	
	public String count(String authCode, String tableName, String data) {
		DBHelper db = new DBHelper(DBName, tableName);
		return jsonShow( authCode, db.where(JSONArray.toJSONArray(data)).count() );
	}

	public String group(String authCode, String tableName, String condString, String field, String method, String groupField, int max) {
		JSONArray array = new JSONArray();
		DBHelper db = new DBHelper(DBName, tableName);
		if (max > 0) {
			db.limit(max);
		}
		db.desc(field).where(JSONArray.toJSONArray(condString));
		switch (method) {
		case "count":
			array = db.count(field).group(groupField);
			break;
		case "avg":
			array = db.avg(field).group(groupField);
			break;
		case "max":
			array = db.max(field).group(groupField);
			break;
		case "min":
			array = db.min(field).group(groupField);
		}
		return arrayShow(authCode,array);

	}

	@SuppressWarnings("unchecked")
	protected String jsonShow(String authCode, long l) {
		JSONArray ja = new JSONArray();
		for (int i = 0; i < l; i++) {
			ja.add(new JSONObject());
		}
		return arrayShow(authCode, ja);
	}

	protected String jsonShow(String authCode, JSONObject rs) {
		if (rs == null) {
			rs = new JSONObject();
		}
		return arrayShow(authCode, (new JSONArray()).adds(rs));
	}

	@SuppressWarnings("unchecked")
	protected String arrayShow(String authCode, JSONArray record) {
		JSONObject rs = new JSONObject();
		JSONObject temp = new JSONObject();
		if (record == null) {
			record = new JSONArray();
		}
		temp.put("data", record);
		temp.put("totalSize", String.valueOf(record.size()));
		rs.put("record", temp);
		rs.put("authCode", getAuthCode(authCode));
		return rs.toJSONString();
	}

	public String auth(String appid, String appSecret) {
		/*
		 * 验证通过以后，生成授权码
		 */
		return jsonShow(getAuthCode(codec.randomString(15)), null);
	}

	private String getAuthCode(String authCode) {
		return codec.randomString(15);
	}
}
