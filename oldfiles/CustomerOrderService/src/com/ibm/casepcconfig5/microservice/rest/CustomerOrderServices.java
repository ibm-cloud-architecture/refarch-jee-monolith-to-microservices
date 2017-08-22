package com.ibm.casepcconfig5.microservice.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.casepcconfig5.microservice.utils.Commons;
import com.ibm.casepcconfig5.microservice.utils.Constants;
import com.ibm.json.java.JSONObject;

@ApplicationPath("/")
@Path("Customer")
public class CustomerOrderServices extends Application {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomer(@HeaderParam("authorization") String authString) {
		return invokeRest(Constants.CUSTOMER_URL, authString, "GET", null, null);
	}

	@PUT
	@Path("/Address")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateAddress(InputStream is, @Context HttpHeaders headers, @HeaderParam("authorization") String authString) {
		return invokeRest(Constants.CUSTOMER_ADDRESS_URL, authString, "PUT", headers, is);
	}

	@POST
	@Path("/OpenOrder/LineItem")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addLineItem(InputStream is, @Context HttpHeaders headers, @HeaderParam("authorization") String authString) {
		return invokeRest(Constants.CUSTOMER_ADDRESS_OPENORDER_LINEITEM_URL, authString, "POST", headers, is);
	}

	@DELETE
	@Path("/OpenOrder/LineItem/{productId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeLineItem(@PathParam(value = "productId") int productId, @Context HttpHeaders headers, @HeaderParam("authorization") String authString) {
		return invokeRest(Constants.CUSTOMER_ADDRESS_OPENORDER_LINEITEM_ID_URL + productId, authString, "DELETE", headers, null);
	}

	@POST
	@Path("/OpenOrder")
	public Response submitOrder(InputStream is, @Context HttpHeaders headers, @HeaderParam("authorization") String authString) {
		return invokeRest(Constants.CUSTOMER_OPENORDER_URL, authString, "POST", headers, is);
	}

	@GET
	@Path("/Orders")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrderHistory(@HeaderParam("authorization") String authString) {
		return invokeRest(Constants.CUSTOMER_ORDERS_URL, authString, "GET", null, null);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/TypeForm")
	public Response getCustomerFormMeta(@HeaderParam("authorization") String authString) {
		return invokeRest(Constants.CUSTOMER_TYPEFORM_URL, authString, "GET", null, null);
	}

	@POST
	@Path("/Info")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateInfo(InputStream is, @Context HttpHeaders headers, @HeaderParam("authorization") String authString) {
		return invokeRest(Constants.CUSTOMER_INFO_URL, authString, "POST", headers, is);
	}
	
	private Response invokeRest(String urlStr, String authString, String method, HttpHeaders headers, InputStream is){
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
			
			if( "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) ){
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
			
			Map<String, List<String>> map = conn.getHeaderFields();
			String eTag = "";
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				if( "ETag".equalsIgnoreCase(entry.getKey())){
					eTag = entry.getValue().get(0);
					break;
				}
			}
			
			if (code == 204) {
				return Response.status(Response.Status.NO_CONTENT).header("ETag", eTag).header("Access-Control-Allow-Origin", "*").build();
			}
			String responseStr = Commons.getStringFromInputStream(conn.getInputStream());
			System.out.println("Response  = " + responseStr);
			if (code == 200) {
				return Response.ok(responseStr, MediaType.APPLICATION_JSON).header("ETag", eTag).header("Access-Control-Allow-Origin", "*").build();
			}
			if( code >= 200 && code < 400 ){
				return Response.status(code).entity(responseStr).header("ETag", eTag).header("Access-Control-Allow-Origin", "*").build();
			}
			if( code >= 400 && code < 500 ){
				return Response.status(code).entity("Client Error").header("ETag", eTag).header("Access-Control-Allow-Origin", "*").build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*").build();
		} catch (MalformedURLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
					.header("Access-Control-Allow-Origin", "*").build();
		} catch (IOException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
					.header("Access-Control-Allow-Origin", "*").build();
		}
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

	private boolean isBasicAuthDataProvided(String authString) {
		System.out.println("AuthString = " + authString);
		if (authString != null && authString.startsWith("Basic")) {
			return true;
		}
		return false;
	}

}
