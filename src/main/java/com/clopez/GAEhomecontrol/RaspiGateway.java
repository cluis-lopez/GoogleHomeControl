package com.clopez.GAEhomecontrol;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.pusher.rest.data.Result;
import com.pusher.rest.data.Result.Status;

/**
 * Este Servlet sólo es accesible por la Raspberry Pi (autentificacion por token) la Raspebrry postea aqui datos
 *  que este servlet re-envia mediante un mensaje Pusher al cliente original.
 *  Para ello este servlet abrirá un canal "datosCliente" y el nombre del evento será el nombre del usuario loggeado que pidió el mensaje 
 *  original.
 */
@WebServlet("/RaspiGateway")
public class RaspiGateway extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RaspiGateway() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Logger log = Logger.getLogger(ClientGateway.class.getName());
		String res="";
		Gson gson = new Gson();
		
		String user = request.getParameter("user");
		String internalPassword = request.getParameter("password");
		String json = request.getParameter("json");
		
		log.log(Level.FINE, "Acceso por: "+ user + " con token: "+ internalPassword + " y datos : "+ json);
		log.log(Level.FINE, "Tamaño de los datos: "+ gson.toJson(json).length());
		
		/* Password de uso inerno que debe estar hardcoded entre este servlet
		 * y el Listener que se ejecuta en la Raspberry Pi
		 */
		
		if (internalPassword.equals(System.getenv("internalPassword"))) {
			Result result = PusherService.getDefaultInstance(log).trigger(
	                "datosCliente",
	                "event_"+user, // name of event
	                gson.toJson(json));
			if (result.getStatus() == Status.SUCCESS)
				res = "OK";
			else
				res = "Pusher Failure";
		} else {
			res = "Unathorized";
		}
		
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("cache-control", "no-cache");
		response.getWriter().write(res);
		response.flushBuffer();
	}

}
