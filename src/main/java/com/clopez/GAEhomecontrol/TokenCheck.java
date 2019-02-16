package com.clopez.GAEhomecontrol;


import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet implementation class CheckUser
 */
@WebServlet("/TokenCheck")
public class TokenCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

			Logger log = Logger.getLogger(TokenCheck.class.getName());
			
			String user = request.getParameter("user");
			String token = request.getParameter("token");
			Gson gson = new Gson();
			HashMap<String, String> mapa = new HashMap<>();
			
			if (DataStore.validToken(user, token, log)) {
				mapa.put("user", user);
				mapa.put("token", token);
			} else {
				mapa.put("user", user);
				mapa.put("token", "INVALID");
			}
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setHeader("cache-control", "no-cache");
			response.getWriter().write(gson.toJson(mapa));
			response.flushBuffer();

	}
}
