package webserver;

import java.util.Date;
import java.util.Map;
import java.util.function.BiFunction;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@ApplicationPath("/") //for the jax container // extends Application
@Path("/")
public class JSONWebService extends WebServiceProcedure{
	private static final Logger LOG = LoggerFactory.getLogger(OseDatabase.class);

	@Resource
	protected WebServiceContext wsContext;
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
			@QueryParam("someParam") int someParam,
			) {// does not work @Context UriInfo info
		JSONObject json;
		try {
			String queryString = (String) wsContext.getMessageContext().
					get(MessageContext.QUERY_STRING);
			Map<String, String> params = splitQueryString(queryString);
			json = get(params);
		} catch (Exception e) {
			LOG.error(e);
			json = error(e.getMessage());
		}
		return Response.ok()
				.entity(json)
				.build();
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doPost(JSONObject request) {
		JSONObject json;
		try {
			json = post(request);
		} catch (Exception e) {
			LOG.error(e);
			json = error(e.getMessage());
		}
		return Response.ok()
				.entity(json)
				.build();
	}
	@Override
	protected JSONObject error(String error) {
		JSONObject json = new JSONObject();
		try {
			if(error != null ) {
				json.put("error", error);
			}else {
				json.put("error", "can't handle this request");
			}
		}catch (Exception e) {
			LOG.error(e);
		}
		return json;
	}

	public JSONObject get(Map<String, String> params) {
		String method = (params.containsKey("METHOD") ) ? params.get("METHOD") : "default";
		return doRequest(
				method,
				params,
				Map.class,
				this::getInteger,
				this::getDate);
	}

	public JSONObject post(JSONObject request) {
		String method;
		try {
			method = (request.has("METHOD") ) ? request.getString("METHOD") : "default";
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return doRequest(
				method,
				request,
				JSONObject.class,
				this::getInteger,
				this::getDate);
	}
	
	private <ParamType> JSONObject doRequest(
			String method,
			ParamType params, 
			Class<ParamType> paramClass,
			TriFunction<String, ParamType, Integer, Integer> getInteger,
			BiFunction<String, ParamType, Date> getDate ){
		try{
      //doStuff with reflection

			if(result instanceof JSONObject){
				return (JSONObject) result;
			}
			return error(method + " requires a JSONObject return type");
			}catch(Exception e){
				LOG.warn("request failed " + e.getMessage() + " " + e.getStackTrace()[0].toString() ) ;
				return error(null);
			}
	}
}
