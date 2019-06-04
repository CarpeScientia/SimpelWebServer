package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class WebServer {
	private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);
	//private Endpoint endpoint;
	private HttpsServer httpsServer;

	public WebServer(String keystoreFile, String keyPass, String hostname, int port, String path) throws Exception{

		//endpoint = Endpoint.create(webService);
		String uri = "https://"+hostname+":"+port+path;
		LOG.info("Starting WebServer with uri " + uri);
		SSLContext ssl = SSLContext.getInstance("TLS");

		KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore store = KeyStore.getInstance("JKS");
		try( InputStream in = WebServer.class.getClassLoader().getResourceAsStream(keystoreFile) ){
			store.load(in, keyPass.toCharArray());
		}

		keyFactory.init(store, keyPass.toCharArray());
		TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustFactory.init(store);

		ssl.init(keyFactory.getKeyManagers(),
				trustFactory.getTrustManagers(),
				new SecureRandom());

		HttpsConfigurator configurator = new HttpsConfigurator(ssl);
		httpsServer = HttpsServer.create(new InetSocketAddress(hostname, port), port);
		httpsServer.setHttpsConfigurator(configurator);

		//no longer works HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(new JaxRsApplication(), HttpHandler.class);
		//HttpContext httpContext = 
		httpsServer.createContext(path, new MyHttpHandler());//createContext(path, handler);

		httpsServer.start();

		//endpoint.publish(httpContext);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				httpsServer.stop(0);
			}
		}));
	}


	private static class MyHttpHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange exc) throws IOException {
			JSONWebService serv = new JSONWebService();
			JSONObject resp;
			try {
				resp = new JSONObject("{\"error\": \"require JSON request\"}");
			} catch (JSONException e1) {
				LOG.error("should not have happened", e1);
				return;
			}
			try{
				JSONObject req = readRequest(exc);
				Map<String, String> params = serv.splitQueryString(exc.getRequestURI());

				switch(exc.getRequestMethod().toUpperCase() ) {
				case "GET":
					resp = serv.get(params);
					break;
				case "POST":
					resp = serv.post(req);
					break;
				default:
					resp = new JSONObject("{\"error\": \"unsupported HTTP method\"}");			
				}
				sendJsonResponse(exc, resp, 200);
			}catch(Exception e){
				LOG.error("Server failed", e);
				sendJsonResponse(exc, resp, 500);
			}

			exc.getRequestBody().close();
			exc.close();

		}

		private void sendJsonResponse(HttpExchange exc, JSONObject resp, int responseCode) throws IOException {
			byte[] toSend = resp.toString().getBytes(StandardCharsets.UTF_8);
			Headers respHeaders = exc.getResponseHeaders();
			respHeaders.add("Content-Type","application/json");
			exc.sendResponseHeaders(responseCode, toSend.length );
			try(OutputStream out = exc.getResponseBody() ){
				out.write(toSend);
			}
		}

	}
	//this wrapping class no longer works
	//	public static class JaxRsApplication extends Application {
	//	    private final Set<Class<?>> classes;
	//
	//	    public JaxRsApplication() {
	//	        Set<Class<?>> c = new HashSet<Class<?>>();
	//	        c.add(JSONWebService.class);
	//	        classes = Collections.unmodifiableSet(c);
	//	    }
	//
	//	    @Override
	//	    public Set<Class<?>> getClasses() {
	//	        return classes;
	//	    }
	//	}

	private static JSONObject readRequest(HttpExchange httpExchange) throws IOException, JSONException {
		StringBuilder body = new StringBuilder();
		if(httpExchange.getRequestBody() != null &&
				! "GET".equals(httpExchange.getRequestMethod().toUpperCase()) ) {
			try (InputStreamReader reader = new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8)) {
				char[] buffer = new char[256];
				int read;
				while ((read = reader.read(buffer)) != -1) {
					body.append(buffer, 0, read);
				}
			}
			return new JSONObject(body.toString() );
		}
		return new JSONObject("{}");
	}

}
