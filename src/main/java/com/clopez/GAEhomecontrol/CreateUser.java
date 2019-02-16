package com.clopez.GAEhomecontrol;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet implementation class CreateUser
 */
@WebServlet("/CreateUser")
public class CreateUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateUser() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	Logger log = Logger.getLogger(ClientGateway.class.getName());
    	String name = request.getParameter("name");
		String pass = request.getParameter("password");
		Gson gson = new Gson();
		String result;
		
		if ((DataStore.createUser(name, pass, log)).equals(name)){
			result = name; // Success creating the user
		} else {
			result = "INVALID";
		}
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("cache-control", "no-cache");
		String json = gson.toJson("{'result' : " + result + "}");
		response.getWriter().write(json);
		response.flushBuffer();
	}


}
