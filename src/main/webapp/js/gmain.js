function main(u, t) {
	
	var user = u;
	var token = t;
	var ipadd;
	const mapaModes = {"OFF":0, "MANUAL":1, "PROGRAMA":2};
	const dias = {0:"Domingo", 1:"Lunes", 2:"Martes", 3:"Miercoles", 4:"Jueves", 5:"Viernes", 6:"Sabado"};
	var swHora = 0;
	let calendario;

	for (var i = 30; i >= 10; i--){
		if (i == 21){
			$("#tempTarget_select").append("<option selected>"+i+"</option>");
			$("#tempPrograma").append("<option selected>"+i+"</option>");
		} else {
			$("#tempTarget_select").append("<option>"+i+"</option>");
			$("#tempPrograma").append("<option>"+i+"</option>");
		}
	};
	
	for (var i=0; i<7; i++){
		var dia = new Date().getDay();
		$("#diaCopia").append("<option selected>"+dias[i]+"</option>");
		if ( i == dia)
			$("#diaPrograma").append("<option selected>"+dias[i]+"</option>");
		else
			$("#diaPrograma").append("<option>"+dias[i]+"</option>");
	}
	
	for (var i=0; i<24; i++){
		var label = "" + i;
		if (label.length <2)
			label = "0" + label;
		$("#horaInicio").append("<option>"+label+":00"+"</option>");
		$("#horaInicio").append("<option>"+label+":30"+"</option>");
		$("#horaFinal").append("<option>"+label+":00"+"</option>");
		$("#horaFinal").append("<option>"+label+":30"+"</option>");
	}
	$("#horaFinal").append("<option>24:00</option>");
	
	$("#username").html("User: "+u);
	
	// Get the current external IP address to log
	$.getJSON('//api.ipify.org?format=jsonp&callback=?', function(data) {
		console.log(JSON.stringify(data, null, 2));
		ipadd = data.ip;
	});
	
	//Initialize Pusher channel
	
	//Pusher.logToConsole = true;
	
	var pusher = new Pusher('35300c34839e8e048296', {
		cluster : 'eu',
		encrypted : true,
	});
	
	var channel = pusher.subscribe('datosCliente');
	
	channel.bind('event_'+user, function(data) {
		x = JSON.parse(JSON.parse(data));
		refresca(x);
	});
	
	
	$("#iconMenu").click(function toggleMenu(){
		if ($("#myLinks").css("display") == "block")
			$("#myLinks").css("display", "none");
		else
			$("#myLinks").css("display", "block");
	});
	
	$("#cont1").resize(resizeCanvas(250/350, $("#chart")[0]));
	$("#cont2").resize(resizeCanvas(150/350, $("#programa")[0]));
	
	sendSignal();
	 
	 $("#monitor_menu").click(function(){
		 $("#myLinks").css("display", "none");
		 $("#monitor").css("display","block");
		 $("#control").css("display", "none");
		 $("#program").css("display","none");
		 $("#historic").css("display","none");
		 $("#about").css("display","none");
		 sendSignal();
	 });
	 
	 $("#control_menu").click(function(){
		 $("#myLinks").css("display", "none");
		 $("#monitor").css("display","none");
		 $("#control").css("display", "block");
		 $("#program").css("display","none");
		 $("#historic").css("display","none");
		 $("#about").css("display","none");
		 $("#modeOp_select").val($("#modeOp").text());
		 if (isNaN($("#tempTarget").text().split(" ")[0])) // True if this is Not a Number
			 $("#tempTarget_select").val("20");
		 else
			 $("#tempTarget_select").val($("#tempTarget").text().split(" ")[0]);
		 if ($("#modeOp").text() == "MANUAL")
			 $("#tempTarget_select").prop("disabled", false);
		 else
			 $("#tempTarget_select").prop("disabled", true);
	 });

	 $("#modeOp_select").change(function(){
		 if ($("#modeOp_select").val() == "MANUAL")
			 $("#tempTarget_select").prop("disabled", false);
		 else
			 $("#tempTarget_select").prop("disabled", true);
	 });
	 
	 $("#control_button").click(function(){
		 $("#control_button").css("background-color","red");
		 $("#control_button").prop("disabled", true);
		 postea(mapaModes[$("#modeOp_select").val()], $("#tempTarget_select").val());
	 });
	
	 $("#program_menu").click(function(){
		 $("#myLinks").css("display", "none");
		 $("#monitor").css("display","none");
		 $("#control").css("display", "none");
		 $("#program").css("display","block");
		 $("#historic").css("display","none");
		 $("#about").css("display","none");
	 });
	 
	 $("#diaPrograma").change(function(){
		 pintaProgramaDia(calendario, $("#programa")[0], getKeyByValue(dias, $(this).val()));
	 });
	 
	 //Los botones para ajustar el programa
	 $("#copiar").click(function(){
		 let origen = getKeyByValue(dias, $("#diaCopia").val());
		 let destino = getKeyByValue(dias, $("#diaPrograma").val());
		 for (let i = 0; i<24; i++){
			 calendario.dias[destino][i][0] = calendario.dias[origen][i][0];
			 calendario.dias[destino][i][1] = calendario.dias[origen][i][1];
		 }
		 pintaProgramaDia(calendario, $("#programa")[0], destino);
	 });
	 
	 $("#rango").click(function(){
		 let horaInicio = parseInt($("#horaInicio").val().split(":")[0]);
		 let horaFinal = parseInt($("#horaFinal").val().split(":")[0]);
		 let minInicio = parseInt($("#horaInicio").val().split(":")[1]);
		 let minFinal = parseInt($("#horaFinal").val().split(":")[1]);
		 let goalTemp = parseInt($("#tempPrograma").val());
		 let dia = getKeyByValue(dias, $("#diaPrograma").val());
		 if (horaInicio > horaFinal || (horaInicio == horaFinal && minInicio >= minFinal))
			 return;
		 let i;
		 for (i = horaInicio; i < horaFinal; i++) {
			 if (i == horaInicio && minInicio >= 30 ) {
				 calendario.dias[dia][i][1] = goalTemp;
				 continue;
			 }
			 calendario.dias[dia][i][0] = calendario.dias[dia][i][1] = goalTemp;
		 }

		 if (minFinal >= 30) {
			 calendario.dias[dia][i][0] = goalTemp;
		 }
		 
		 pintaProgramaDia(calendario, $("#programa")[0], dia);
	 });
	 
	 $("#program_button").click(function(){
		 $("#refrescando").css("display", "block");
		 $("#program_button").text("Activando");
		 var datos = {program: calendario.dias};
		 $.ajax({ cache: false,
			 url: "ClientGateway",
			 data: {"token": token,
				 "user": user,
				 "command": "PROGRAMAR",
				 "data": JSON.stringify(datos),
				 "socketId": pusher.connection.socket_id,
			 },
			 type: 'post',
			 success: function (data) {
				 if (data == "OK"){
					 $("#refrescando").css("display", "none");
					 $("#program_button").css("background-color","lightskyblue");
					 $("#program_button").text("Activar")
					 $("#program_button").prop("disabled", false);
				 } else {
					 $("#refrescando").css("display", "none");
					 $("#program_button").css("background-color","lightskyblue");
					 $("#program_button").text("Activar")
					 $("#program_button").prop("disabled", false);
					 alert("Algo ha ido mal al conectarnos con el sistema");
				 }
			 }, 
			 error: function (xhr, ajaxOptions, thrownError) {
				 $("#refrescando").css("display", "none");
				 $("#program_button").css("background-color","lightskyblue");
				 $("#program_button").text("Activar")
				 $("#program_button").prop("disabled", false);
				 alert("Ha habido un problema al comunicarse con la app Google\n\n" + thrownError);
			 },
			 timeout: 15000
		 });
	 });
	 
	 $("#about_menu").click(function(){
		 $("#myLinks").css("display", "none");
		 $("#monitor").css("display","none");
		 $("#control").css("display", "none");
		 $("#program").css("display","none");
		 $("#historic").css("display","none");
		 $("#about").css("display","block");
	 });
	 
	 function sendSignal() {
		 $("#refrescando").css("display", "block");
		 $.ajax({ cache: false,
			    url: "ClientGateway",
			    data: {"token": token,
						"user": user,
						"command": "GET",
						"data": "",
						"socketId": pusher.connection.socket_id,
						},
				type: 'post',
			    success: function (data) {
			    	if (data != "OK"){
			    		console.log("Algo ha fallado al enviar datos a Pusher" + data);
			    	}
			    }, 
			    error: function (xhr, ajaxOptions, thrownError) {
			    	$("#refrescando").css("display", "none");
			        alert("Ha habido un problema al comunicarse con la app Google\n\n" + thrownError);
			    },
			    timeout: 15000
			});
		}
	 
	 function refresca(data) {
//		 $("#refrescando").css("display", "block");
		 $("#estado").text(data.estado);
		 $("#currentTemp").text(data.currentTemp + " \260C");
		 $("#currentHum").text(data.currentHum + " %");
		 modo = Object.keys(mapaModes)[data.modeOp];
		 $("#modeOp").text(modo);
		 if (data.tempTarget == "9999") {
			 $("#tempTarget").text("N.A.");
			 $("#tempTarget2").text("N.A.");
		 } else {
			 $("#tempTarget").text(data.tempTarget+" \260C");
			 $("#tempTarget2").text(data.tempTarget+" \260C");
		 }
		 $("#refrescando").css("display", "none");
		 pintaChart(data.chart, $("#chart")[0]);
		 calendario = data.calendario;
		 pintaPrograma(data.calendario, $("#programa")[0]);
	 }
	 
	 
	 function lastChart(){
	 	$.get("HistoryServlet?mode=last", function(responseJson) {
			pintaChart(responseJson, $("#chart")[0]);
		});
	};
	
	function postea(modeClient, tempClient){
		$("#refrescando").css("display", "block");
		$("#control_button").text("Activando");
		var datos = {clientMode: modeClient.toString(), clientTemp: tempClient.toString()};
		$.ajax({ cache: false,
		    url: "ClientGateway",
		    data: {"token": token,
					"user": user,
					"command": "CONTROL",
					"data": JSON.stringify(datos),
					"socketId": pusher.connection.socket_id,
					},
			type: 'post',
		    success: function (data) {
				if (data == "OK"){
					$("#refrescando").css("display", "none");
					$("#control_button").css("background-color","lightskyblue");
					$("#control_button").text("Activar")
					$("#control_button").prop("disabled", false);
				} else {
					$("#refrescando").css("display", "none");
					$("#control_button").css("background-color","lightskyblue");
					$("#control_button").text("Activar")
					$("#control_button").prop("disabled", false);
					alert("Algo ha ido mal al conectarnos con el sistema");
				}
		    }, 
		    error: function (xhr, ajaxOptions, thrownError) {
		    	$("#refrescando").css("display", "none");
		    	$("#control_button").css("background-color","lightskyblue");
				$("#control_button").text("Activar")
				$("#control_button").prop("disabled", false);
		        alert("Ha habido un problema al comunicarse con la app Google\n\n" + thrownError);
		    },
		    timeout: 15000
		});
	}
	 
 };
	