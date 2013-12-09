package com.goeuro.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.goeuro.sslconnection.SSLSocket;


public class Main {

	public static void main(String[] args) {
		try {
			String location;
			creatingSSLSocket();
			if (args.length != 1) {
				throw new IllegalArgumentException(
						"Incorrect numer of command line arguments");
			} else {
				location = args[0];
				stringValidation(location);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void creatingSSLSocket() throws NoSuchAlgorithmException,
			KeyManagementException {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[] { new SSLSocket() }, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(ctx
					.getSocketFactory());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static void stringValidation(String location) {
		try {
			if (location != null) {
				if (!location.matches(".*\\d.*")
						&& location.matches("[a-zA-Z]*")) {
					getJsonToCsv(location);
				} else {
					throw new Exception(
							"Please put only character in your string");
				}
			} else {
				throw new NullPointerException(
						"Location is null @ com.goeuro.main.stringValidation");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static void getJsonToCsv(String location) {
		try {
			try {
				File file = new File("Data.csv");
				URL url = new URL(
						"https://api.goeuro.de/api/v1/suggest/position/en/name/"
								+ location);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : "
							+ conn.getResponseCode());
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(conn.getInputStream())));
				String jsonResponse;
				while ((jsonResponse = br.readLine()) != null) {
					JSONObject output = new JSONObject(jsonResponse);
					JSONArray docs = output.getJSONArray("results");
					for (int i = 0; i < docs.length(); i++) {
						JSONObject geo_pos = (JSONObject) (docs
								.getJSONObject(i).getJSONObject("geo_position"));
						docs.getJSONObject(i).put("latitude",
								geo_pos.get("latitude"));
						docs.getJSONObject(i).put("longitude",
								geo_pos.get("longitude"));
						docs.getJSONObject(i).remove("geo_position");
					}

					String csv = CDL.toString(docs);
					FileUtils.writeStringToFile(file, csv);
				}
				conn.disconnect();

			} catch (MalformedURLException e) {
				e.printStackTrace();

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
