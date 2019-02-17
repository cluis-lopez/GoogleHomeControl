package com.clopez.GAEhomecontrol;


import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class DataStore {

	static DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

	public static boolean validToken(String user, String token, Logger log) {
		Entity e;
		try {
			e = ds.get(KeyFactory.createKey("User", user));
			Date d = new Date();
			Date dt = (Date) e.getProperty("TokenValidUpTo");
			if (d.getTime() > dt.getTime()) {
				log.log(Level.INFO, "Usuario válido pero el token ha expirado. Se requiere login");
				return false;
			}
			if (String.valueOf(e.getProperty("Token")).equals(token)) {
				return true;
			}
		} catch (EntityNotFoundException e1) {
			log.log(Level.INFO, "El usuario {0} no existe. Este error no debería producirse", user);
			return false;
		}
		return false;
	}
		
	
	public static String createUser(String name, String password, Logger log) {
		Key k = KeyFactory.createKey("User", name);
		Filter propertyFilter = new FilterPredicate("__key__", FilterOperator.EQUAL, k);
		Query q = new Query("User").setFilter(propertyFilter);
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withLimit(1));

		if ( ! ents.isEmpty()) { // At least, one user with this name already exists in the database. Return.
			log.log(Level.WARNING, "{0} es un usuario ya registrado", name);
			return "INVALID";
		}
		// No users with this username, we may create one
		
		Entity e = new Entity("User", name);
		Date d = new Date();
		Date old = new Date(0);
		String[] pass = new String[2];
		try {
			pass = Encrypt.hashPasswd(password);
		} catch (NoSuchAlgorithmException e1) {
			log.log(Level.SEVERE, e1.getMessage());
		}
		
		e.setProperty("Password", pass[0]);
		e.setProperty("Salt", pass[1]);
		e.setProperty("UserSince", d);
		e.setProperty("LastLogin", null);
		e.setProperty("TokenValidUpTo", old);
		ds.put(e);
		
		return name;
	}
	
	public static String[] loginUser(String user, String passwd, Logger log) {
		Entity e;
		String[] ret = {"INVALID", "INVALID"}; //ret[0] : status o el usuario; ret[1]: el token
		
		try {
			e = ds.get(KeyFactory.createKey("User", user));
			if ( e!= null && Encrypt.checkPasswd(passwd, (String) e.getProperty("Password"), (String) e.getProperty("Salt"))) {
				// Existe el usuario y la password coincide con la almacenada
				ret[0] = user;
				Date d = new Date();
				//Chequeamos si hay un token almacenado y si este es aún válido
				String token = (String) e.getProperty("Token");
				if (token != null && validToken(user, token, log)) {
					//El token es válido
					log.log(Level.INFO, "El usuario " + user + " ya tiene un token valido: " + token);
					ret[1] = token;
				} else { //No hay token o ha expirado.Therefore, generamos uno nuevo
					UUID uuid = UUID.randomUUID();
					long t = d.getTime();
					t = t + 7 * 24 * 60 * 60 * 1000; // Tiempo de validez del token: 7 días
					Date valid = new Date(t);
					ret[1] = uuid.toString();
					e.setProperty("Token", uuid.toString());
					e.setProperty("TokenValidUpTo", valid);
				}
				e.setProperty("LastLogin", d);
				ds.put(e);
			} else {
				//El usuario existe pero la password no es correcta
				ret[0] = "Invalid password";
			}
		} catch (EntityNotFoundException e1) {
			ret[0]="Invalid user";
		}
		return ret;
	}
	
}
