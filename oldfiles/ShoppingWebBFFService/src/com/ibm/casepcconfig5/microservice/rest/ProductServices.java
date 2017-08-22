package com.ibm.casepcconfig5.microservice.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.casepcconfig5.microservice.utils.Commons;
import com.ibm.casepcconfig5.microservice.utils.Constants;
import com.ibm.json.java.JSONObject;

@ApplicationPath("/rest")
@Path("Product")
public class ProductServices extends Application {

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProduct(@HeaderParam("authorization") String authString, @PathParam(value = "id") int productId) {
		return invokeRest(Constants.PRODUCT_ID_URL + productId, authString, "GET", null, null);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProductsByCategory(@HeaderParam("authorization") String authString, @QueryParam(value="categoryId") int categoryId) {
		return invokeRest(Constants.PRODUCT_CATEGORY_ID_URL + categoryId, authString, "GET", null, null);
	}

	private Response invokeRest(String urlStr, String authString, String method, HttpHeaders headers, InputStream is) {
		JSONObject responseJSON = new JSONObject();

		if (!isBasicAuthDataProvided(authString)) {
			responseJSON.put("ResponseMessage", "No Authentication data provided");
		}

		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn = setRequestProperty(conn, authString, method);

			if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
				List<String> ifMatchValue = headers.getRequestHeader("If-Match");

				if (ifMatchValue != null) {
					conn.setRequestProperty("If-Match", headers.getRequestHeader("If-Match") != null
							? headers.getRequestHeader("If-Match").get(0) : "");
				}

				JSONObject inputJSON = JSONObject.parse(is);
				OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
				System.out.println("InputData = " + inputJSON.toString());
				writer.write(inputJSON.toString());
				writer.close();
			}

			int code = conn.getResponseCode();
			System.out.println("Request URL = " + urlStr);
			System.out.println("Response code = " + code);

			if (code == 204) {
				return Response.status(Response.Status.NO_CONTENT).header("Access-Control-Allow-Origin", "*").build();
			}
			String responseStr = Commons.getStringFromInputStream(conn.getInputStream());
			System.out.println("Response  = " + responseStr);
			if (code == 200) {
				return Response.ok(responseStr, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*")
						.build();
			}
			if (code >= 200 && code < 400) {
				return Response.status(code).entity(responseStr).header("Access-Control-Allow-Origin", "*").build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*")
					.build();
		} catch (MalformedURLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
					.header("Access-Control-Allow-Origin", "*").build();
		} catch (IOException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
					.header("Access-Control-Allow-Origin", "*").build();
		}
	}

	private boolean isBasicAuthDataProvided(String authString) {
		if (authString == null || !authString.startsWith("Basic")) {
			return true;
		}
		return false;
	}

	private HttpURLConnection setRequestProperty(HttpURLConnection conn, String authString, String type)
			throws ProtocolException {
		conn.setRequestProperty("authorization", authString);
		conn.setRequestMethod(type);
		conn.setRequestProperty("Accept", "application/json");
		if (!"GET".equals(type) && !"DELETE".equals(type)) {
			conn.setRequestProperty("Content-Type", "application/json");
		}
		return conn;
	}

}
