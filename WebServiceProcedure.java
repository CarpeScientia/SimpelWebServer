package webserver;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WebServiceProcedure{
	private static final Logger LOG = LoggerFactory.getLogger(WebServiceProcedure.class);

	@FunctionalInterface
	protected interface TriFunction<A,B,C,R> {

	    R apply(A a, B b, C c) throws Exception;
	}
	
	protected Integer getInteger(String key ,Map<String, String> params ) {
		String value = params.get(key);
		if(value != null) {
			try {
				return Integer.valueOf(value);
			}catch(NumberFormatException e) {
				LOG.warn(value + " is not a number " + e.getMessage());
			}
		}
		return null;
	}

	protected Integer getInteger(String key ,Map<String, String> params, int defaultValue ) {
		Integer value = getInteger(key, params);
		if(value == null){
			return defaultValue;
		}
		return value;
	}
	
	protected Integer getInteger(String key, JSONObject params, int defaultValue ) {
		Integer value;
		if( !params.has(key)){
			return defaultValue;
		}
		try {
			value = params.getInt(key);
		} catch (JSONException e) {
			return defaultValue;
		}
		return value;
	}
	
	protected Date getDate(String key, JSONObject params) {
		String value;
		if( !params.has(key)){
			return null;
		}
		try {
			value = params.getString(key);
		} catch (JSONException e) {
			return null;
		}
		return extractDate(value);
	}
	
	protected Date getDate(String key, Map<String, String> params) {
		String value = params.get(key);
		return extractDate(value);
	}

	protected Date extractDate(String value) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//2019-05-31
		if(value != null) {
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				LOG.warn(value + " could not parse date " + e.getMessage());
			}
		}
		return null;
	}
	
	protected Map<String, String> splitQueryString(URI uri) {
		String query = "";
		if(uri != null) {
			query = uri.getQuery();
		}
		return splitQueryString(query);
	}
	
	protected Map<String, String> splitQueryString(String queryString) {
		Map<String, String> params = new HashMap<>();
		if(queryString == null) {
			return params;
		}
		String[] getParams=queryString.split("&");

		for(String param : getParams ) {
			String keyValue[] = param.split("=");
			params.put(keyValue[0].toUpperCase(), keyValue[1]);
		}
		return params;
	}
	
	protected String getString(String key, Map<String, String> params, boolean isMandatory) throws Exception{
		String value = params.get(key);
		if(value == null && isMandatory ){
			throw new Exception(key + "is mandatory");
		}
		return value;
	}
	
	protected String getString(String key, JSONObject params, boolean isMandatory) throws Exception{
		if(! params.has(key) && isMandatory ){
			throw new Exception(key + "is mandatory");
		}
		return params.getString(key);
	}
	
	protected JSONObject error(String error) {
		JSONObject json = new JSONObject();
		try {
			json.put("error", error);
		} catch (JSONException e1) {
			throw new RuntimeException("Undocumented JSONException", e1);
		}
		return json;
	}
}
