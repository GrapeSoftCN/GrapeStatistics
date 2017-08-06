package interfaceApplication;

import java.io.FileInputStream;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.jGrapeFW_Message;
import apps.appsProxy;
import database.db;
import model.StatisticsModel;
import nlogger.nlogger;
import string.StringHelper;

public class StatisticsTest {
	private StatisticsModel model = new StatisticsModel();

	private db getdb(String tableName) {
		return model.bind(tableName);
	}

	/**
	 * 获取当天的统计量或者根据条件获取当天的数据量
	 * 
	 * @project GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param tableName
	 *            表名称
	 * @param condString
	 *            入参格式：[{"field":"","logic":"","value":""},...]
	 *            如：[{"field":"time","logic":">=","value":"1499755452800"},{
	 *            "field":"time","logic":"<","value":"1499788799000"}]/type
	 * @return 出参格式：{"message":"","errorcode":0}
	 *
	 */
	public String CurrentTimeCount(String tableName, String condString) {
		long count = 0;
		JSONArray array = JSONArray.toJSONArray(condString);
		if (array != null && array.size() != 0) {
			count = getdb(tableName).where(array).count();
		}
		return ResultMessage(count);

	}

	/**
	 * 根据条件获取当天的数据量
	 * 
	 * @project GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param tableName
	 *            表名称
	 * @param key
	 *            条件字段名称
	 * @param condString
	 *            入参格式：[{"field":"","logic":"","value":""},...]/key
	 *            如：[{"field":"time","logic":">=","value":"1499755452800"},{
	 *            "field":"time","logic":"<","value":"1499788799000"}]/type
	 * @return 出参格式：{"message":[{"count":"","_id":""},{
	 *         "count":"0.32","_id":""}],"errorcode":0}
	 *
	 */
	public String CurrentTimeCountByCond(String tableName, String key, String condString) {
		long count = 0;
		JSONArray array = JSONArray.toJSONArray(condString);
		JSONArray array2 = null;
		try {
			if (array == null || array.size() == 0) {
				return ResultMessage(count);
			}
			array2 = getdb(tableName).where(array).count(key).group(key);
			String SDKUrl = IsgetSDKService(tableName, key);
			if (!SDKUrl.equals("")){
				array2 = getReportType(array2,SDKUrl);
			}
		} catch (Exception e) {
			nlogger.logout(e);
			array2 = null;
		}
		return ResultMessage(array2);
	}

	/**
	 * 根据条件字段统计数据量
	 * 
	 * @project GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param tableName
	 *            表名称
	 * @param field
	 *            分组[条件]字段名称，如按类型统计，则入参值为type
	 * @return 出参格式：{"message":[{"count":"","_id":{"$numberLong":"0"}},{
	 *         "count":"","_id":{"$numberLong":"1"}}],"errorcode":0}
	 *
	 */
	public String CondCount(String tableName, String field) {
		JSONArray array = null;
		try {
			array = getdb(tableName).count(field).group(field);
		} catch (Exception e) {
			nlogger.logout(e);
			array = null;
		}
		return ResultMessage(array);
	}

	public void WeekCount() {

	}

	public void MonthCount() {

	}

	/**
	 * 获取满足条件的数据量与数据总量的比例
	 * 
	 * @project GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param tableName
	 *            表名称
	 * @param key
	 *            字段名称
	 * @return {"message":[{"count":"0.68","_id":{"$numberLong":"0"}},{
	 *         "count":"0.32","_id":{"$numberLong":"1"}}],"errorcode":0}
	 *
	 */
	@SuppressWarnings("unchecked")
	public String Percent(String tableName, String key) {
		JSONArray array = null;
		JSONObject object;
		float count = Float.parseFloat(Count(tableName)+"");
		float modeCount;
		// 满足条件的数据量
		String value = CondCount(tableName, key);
		object = JSONObject.toJSON(value);
		if (object != null) {
			try {
				array = JSONArray.toJSONArray(object.get("message").toString());
				if (array != null && array.size() != 0 && count != 0) {
					for (int i = 0; i < array.size(); i++) {
						object = (JSONObject) array.get(i);
						modeCount =  Float.parseFloat(object.getLong("count")+"");
						object.put("count", String.format("%.2f", (double) (modeCount / count)));
						array.set(i, object);
					}
				} else {
					array = null;
				}

			} catch (Exception e) {
				nlogger.logout(e);
				array = null;
			}
		}
		return ResultMessage(array);
	}

	/**
	 * 根据表名判断是否需要根据字段代表的id值获取名称
	 * 
	 * @project GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param array
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	private JSONArray getReportType(JSONArray array,String SDKUrl) {
		String rtid = "";
		String tid;
		JSONObject object;
		int l = array.size();
		for (int i = 0; i < l; i++) {
			object = (JSONObject) array.get(i);
			tid = object.getString("_id");
			rtid += (!tid.equals("0")) ? (tid + ",") : rtid;
		}
		if (rtid.length() > 0) {
			rtid = StringHelper.fixString(rtid, ',');
//			String temp = appsProxy.proxyCall(getHost(0), appsProxy.appid() + SDKUrl + rtid, null, null)
//					.toString();
//			JSONObject tempObj = JSONObject.toJSON(temp);
			JSONObject tempObj = null;
			if (tempObj != null) {
				for (int i = 0; i < l; i++) {
					object = (JSONObject) array.get(i);
					object.put("_id", tempObj.get(object.getString("_id")));
					array.set(i, object);
				}
			}
		}
		return array;
	}

	/**
	 * 数据总量统计
	 * 
	 * @project GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param tableName
	 *            表名称
	 * @return
	 *
	 */
	private long Count(String tableName) {
		return getdb(tableName).count();
	}

	/**
	 * 判断是否需要调用第三方服务，不需要调用，则返回"";
	 * @project	GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param tableName
	 * @param key
	 * @return
	 *
	 */
	private String IsgetSDKService(String tableName, String key) {
		String SDKUrl = "";
		switch (tableName) {
		case "reportInfo":
			if (key.equals("type")) {
				SDKUrl = getSDKService(tableName);
			}
			break;

		default:
			break;
		}
		return SDKUrl;
	}

	/**
	 * 获取第三方服务地址
	 * @project	GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param key
	 * @return
	 *
	 */
	private String getSDKService(String key) {
		String value = "";
		try {
			Properties pro = new Properties();
			pro.load(new FileInputStream("SDKConfig.properties"));
			value = pro.getProperty(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}

	private String getAppIp(String key) {
		String value = "";
		try {
			Properties pro = new Properties();
			pro.load(new FileInputStream("URLConfig.properties"));
			value = pro.getProperty(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}

	/**
	 * 获取应用url[内网url或者外网url]，0表示内网，1表示外网
	 * 
	 * @project GrapeStatistics
	 * @package interfaceApplication
	 * @file Statistics.java
	 * 
	 * @param signal
	 * @return
	 *
	 */
	private String getHost(int signal) {
		String host = null;
		try {
			if (signal == 0 || signal == 1) {
				host = getAppIp("host").split("/")[signal];
			}
		} catch (Exception e) {
			nlogger.logout(e);
			host = null;
		}
		return host;
	}

	private String ResultMessage(long num) {
		return ResultMessage(String.valueOf(num));
	}

	private String ResultMessage(JSONArray array) {
		if (array == null) {
			array = new JSONArray();
		}
		return ResultMessage(array.toJSONString());
	}

	private String ResultMessage(String message) {
		return jGrapeFW_Message.netMSG(0, message);
	}
}
