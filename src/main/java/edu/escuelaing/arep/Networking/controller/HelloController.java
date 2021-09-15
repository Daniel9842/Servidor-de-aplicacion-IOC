package edu.escuelaing.arep.Networking.controller;

public class HelloController {
	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}
}
