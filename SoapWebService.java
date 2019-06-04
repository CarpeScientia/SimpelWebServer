package webserver;

import java.io.StringReader;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServiceProvider
@ServiceMode(value = javax.xml.ws.Service.Mode.MESSAGE)
@BindingType(value = HTTPBinding.HTTP_BINDING)
public class SoapWebService extends WebServiceProcedure implements Provider<Source> {

	private static final Logger LOG = LoggerFactory.getLogger(SoapWebService.class);
  
	@Resource
	protected WebServiceContext wsContext;

	@Override
	public Source invoke(Source request) throws WebServiceException {
		MessageContext msg_cxt = wsContext.getMessageContext();
		String httpMethod = (String) msg_cxt
				.get(MessageContext.HTTP_REQUEST_METHOD);
		//System.out.println("Http Method : " + httpMethod);
		if (httpMethod.equalsIgnoreCase("GET")) {
			return doGet(msg_cxt);
		}else if(httpMethod.equalsIgnoreCase("POST")) {
			return doPost(wsContext.getMessageContext(), (StreamSource) request );
		}
		return null;
	}
	private StreamSource doPost(MessageContext messageContext, StreamSource request) {
		return xmlError("Work in progress");

	}
	@SuppressWarnings("unused")
	private StreamSource doGet(MessageContext msg_cxt) {
		String query_string = (String) msg_cxt.get(MessageContext.QUERY_STRING);
		//String path = (String) msg_cxt.get(MessageContext.PATH_INFO);
		StringBuffer text=new StringBuffer("");				
		try {
			Map<String, String> params = splitQueryString(query_string);
      //do stuff
			return xmlError("Work in progress");

		} catch (Exception e) {
			LOG.error(e);
			return xmlError(e.getMessage());

		}
	}


	private StreamSource xmlError(String error) {
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
		try {
			if(error != null) {
				xml.append("<error>" + error + "</error>");
			}else {
				xml.append("<info>Parameters SUBJECTID, SUBJECTTPID and PROCID are required</info>");
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		return new StreamSource(new StringReader(xml.toString()));
	}
  // requires javax.mail
  //	private DataSource getDataSource(String sret)
	//	{
	//	    ByteArrayDataSource ds = new ByteArrayDataSource(sret.getBytes(), "application/json");
	//	    return ds;
	//	}
	

}
