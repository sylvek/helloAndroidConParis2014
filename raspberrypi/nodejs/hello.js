// Load the http module to create an http server.
var http = require('http');
var exec = require('child_process').exec;
var regex = /^temp=(.+)'C/

// Configure our HTTP server to respond with Hello World to all requests.
var server = http.createServer(function (request, response) {
  response.writeHead(200, {"Content-Type": "text/plain"});
  exec("/opt/vc/bin/vcgencmd measure_temp", function(error, stdout, stderr) { 
	var result = stdout.match(regex);
	response.end(result[1]); 
  });
});

// Listen on port 8000, IP defaults to 127.0.0.1
server.listen(8000);

// Put a friendly message on the terminal
console.log("Server running at http://127.0.0.1:8000/");
