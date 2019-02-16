$(document).ready(function() {
	
	if (localStorage.getItem("user") && localStorage.getItem("token")){
		//$("#username").html("<b>User:</b> " + localStorage.getItem("user"));
		tokenCheck(localStorage.getItem("user"), localStorage.getItem("token"));
	} else {
		//$("#username").html("User: No Valid User");
		$("#modallogin").css("display", "block");
	};
	
	function tokenCheck(u, t) {
		$.ajax({
				url: "/TokenCheck",
				type: "POST",
				data: {user: u, token: t},
				dataType: "json",
				success: function (data,status,xhr){
					if(data.user == u && data.token == t){ //Token is OK
						main(u,t);
					} else { //User must login again
						$("#modallogin").css("display", "block");
					}
				}
			});
	};
	

	$("#loginbutton").click(function(){
		u = $("#user").val();
		p = $("#password").val();
		$.ajax({
			url: "/LoginUser",
			type: "POST",
			data: {user: u, password: p},
			dataType: "json",
			success: function (data,status,xhr){
				console.log(data);
				if(data.user == u && data.token != "INVALID"){ //Token is OK
					localStorage.setItem("user", data.user);
					localStorage.setItem("token", data.token);
					location.reload();
				} else { //User must login again
					$("#user").val("");
					$("#password").val("");
					alert("User: "+data.user +"\nStatus: "+data.status);
					$(".login").css("display", "block");
				}
			},
			error: function(xhr, status, message){
				console.log("Fallo en la conexion AJAX "+message);
			}
		});
	});
	 
 });
	