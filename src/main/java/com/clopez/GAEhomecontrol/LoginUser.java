package com.clopez.GAEhomecontrol;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clopez.GAEhomecontrol.DataStore;
import com.google.gson.Gson;

/**
 * Servlet implementation class LoginUser
 */
@WebServlet("/LoginUser")
public class LoginUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Logger log = Logger.getLogger(LoginUser.class.getName());
		
		String user = request.getParameter("user");
		String passwd = request.getParameter("password");
		Gson gson = new Gson();
		HashMap<String, String> mapa = new HashMap<>();
		
		log.log(Level.INFO, "Intento de login. Usuario: {0}", user);
		
		String[] res = new String[2];
		
		if (user != null && passwd != null) {
			res = DataStore.loginUser(user, passwd, log);
			if (! res[0].equals(user)) {
				mapa.put("user", user);
				mapa.put("token", "INVALID");
				mapa.put("status", res[0]);
				log.log(Level.INFO, "Problema: {0}", res[0]);
			} else {
				mapa.put("user", user);
				mapa.put("token", res[1]);
				mapa.put("status", "OK");
				log.log(Level.INFO, "Generado token para el usuario: {0}", user);
			}
		} else {
			mapa.put("user", "INVALID");
			mapa.put("token", "INVALID");
			mapa.put("status", "Invalid request");
			log.log(Level.INFO, "Datos invalidos");
		}
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("cache-control", "no-cache");
		response.getWriter().write(gson.toJson(mapa));
		response.flushBuffer();
	}

}
