package edu.escuelaing.arep.Networking.HttpServer;

import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;


//Code implemented in class with the teacher

/**
 * this class creates a server that returns html files and images
 * @author Daniel Santiago Ducuara Ardila
 *
 */
public class HttpServer {
	
	private static HttpServer _instance = new HttpServer();
	/**
	 * constructor of the httpserver class
	 */
	private HttpServer() {
	}
	/**
	 * this method creates an instance of httpserver
	 * @return a variable of type httpserver
	 */
	public static HttpServer getInstance() {
		return _instance;
	}
	/**
	 * Heroku port
	 * @return the port
	 */
	public static int getPort() {
		 if (System.getenv("PORT") != null) {
			 return Integer.parseInt(System.getenv("PORT"));
			 }
			 return 35000;
	}
	
	/**
	 * this method orders the execution of starting the server
	 * @param args server variable
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public static void main(String[] args) throws IOException {
		HttpServer.getInstance().startServer(args);
	}
	/**
	 * this method starts the connection to the server
	 * @param args server variable
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void startServer(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(getPort());
		} catch (IOException e) {
			System.err.println("Could not listen on port:");
			System.exit(1);
		}
		Socket clientSocket = null;
		boolean running=true;
		while (running) {
			try {
				System.out.println("Listo para recibir ...");
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Accept failed.");
				System.exit(1);
			}
			processRequest(clientSocket);
		}
		serverSocket.close();
	}
	/**
	 * this method makes a request to the server
	 * @param clientSocket is the connection to the server
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void processRequest(Socket clientSocket) throws IOException {
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String inputLine;
		String method="";
		String path="";
		String version="";
		List<String> headers = new ArrayList<String>();
		while ((inputLine = in.readLine()) != null) {
			if(method.isEmpty()) {
				String[] requestStrings = inputLine.split(" ");
				method = requestStrings[0];
				path = requestStrings[1];
				version = requestStrings[2];
				System.out.println("RequestPath: "+ path);
			}else {
				headers.add(inputLine);
			}
			
			if (!in.ready()) {
				break;
			}
		}
		OutputStream outStream = clientSocket.getOutputStream();
		String responseMsg = createResponse(path, outStream);
		out.println(responseMsg);
		out.close();
		in.close();
		clientSocket.close();
	}
	/**
	 * this method creates a response with the data from the web page
	 * @param path the website address
	 * @return a string with the page content
	 */
	public String createResponse(String path,OutputStream outStream) {
		String type = "text/html";
		String typeimage = "";
		if(path.endsWith(".css")) {
			type = "text/css";
		}else if(path.endsWith(".js")) {
			type = "text/javascript";
		}else if(path.endsWith(".png")) {
			type = "image/png";
			typeimage = "png";
			try {
				createImageResponse(path,outStream,type,typeimage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(path.endsWith(".jpg") || path.endsWith(".jfif")) {
			type = "image/jpg";
			typeimage = "jpg";
			try {
				createImageResponse(path,outStream,type,typeimage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return createTextResponse(path,type);
	}
	/**
	 * this method returns the response from a file
	 * @param path the website address
	 * @param type of file
	 * @return response html
	 */
	public String createTextResponse(String path, String type) {
		Path file = Paths.get("./TestHttpServer"+path);
		Charset charset = Charset.forName("UTF-8");
		String outmsg="";
		try(BufferedReader reader = Files.newBufferedReader(file,charset)){
			String line = null;
			while((line=reader.readLine())!=null) {
				System.out.println(line);
				outmsg = outmsg + "\r\n"+ line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return"HTTP/1.1 200 OK\r\n"
                + "Content-Type: "+ type +"\r\\n"
                + "\r\n"
                + outmsg;
	}
	/**
	 *  this method returns the response from a image file
	 * @param path the website address
	 * @param outStream OutputStream client
	 * @param type of file
	 * @param typeimage of image
	 * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
	 */
	public void createImageResponse(String path,OutputStream outStream, String type,String typeimage) throws IOException {
		BufferedImage image = null;
		File file = new File("./TestHttpServer"+path);
		image = new BufferedImage(240, 240,BufferedImage.TYPE_INT_ARGB);
		image = ImageIO.read(file);
		ByteArrayOutputStream imgbytes = new ByteArrayOutputStream();
        DataOutputStream imgRspse = new DataOutputStream(outStream);
		ImageIO.write(image, typeimage, imgbytes);
		imgRspse.writeBytes("HTTP/1.1 200 OK \r\n"
	            + "Content-Type: "+type+" \r\n"
	            + "\r\n");
		imgRspse.write(imgbytes.toByteArray());        
	}
}
