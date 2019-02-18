package com.clopez.GAEhomecontrol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pusher.rest.data.Result;
import com.pusher.rest.data.Result.Status;

/**
 * Este Servlet escucha request POST de un cliente web. El cliente postea una orden (URI) que este servlet envia mediante un mensaje Pusher
 * al servidor Listener en la Raspberry Pi. Para ello se abre el canal "datosToRaspi" y se envían eventos de tipo "ordenFromWebClient"
 */
@WebServlet("/ClientGateway")
public class ClientGateway extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClientGateway() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Logger log = Logger.getLogger(ClientGateway.class.getName());

		String token = request.getParameter("token");
		
		Map<String, String> mapa = new HashMap<>();
		mapa.put("user", request.getParameter("user"));
		mapa.put("command", request.getParameter("command"));
		mapa.put("data", request.getParameter("data"));
		String socketId = request.getParameter("socketId");
		
		log.log(Level.INFO, "Usuario: "+mapa.get("user")+" con token: "+token+" comando "+mapa.get("command"));
		log.log(Level.FINE, "Datos: "+mapa.get("data"));

		String res = "";
		
		if (DataStore.validToken(mapa.get("user"), token, log)) {
			Result result = PusherService.getDefaultInstance(log).trigger(
		                "datosToRaspi",
		                "ordenFromWebClient", // name of event
		                mapa,
		                socketId); // (Optional) Use client socket_id to exclude the sender from receiving the message
			if (result.getStatus() == Status.SUCCESS) {
					res = "OK";
			} else {
				log.log(Level.INFO, "Pusher Failure. Status: " + result.getStatus()+" Error: "+result.getMessage());
				res = "Pusher Failure. Status: " + result.getStatus()+" Error: "+result.getMessage();
			}
		} else {
			res = "Unauthorized";
		}
		
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("cache-control", "no-cache");
		response.getWriter().write(res);
		response.flushBuffer();
	}

}
