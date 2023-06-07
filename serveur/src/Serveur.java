import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

public class Serveur {

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// Lecture fichier xml
		LectureXML xml = new LectureXML("config/webconf.xml");
		Log logAccess = new Log(xml.getAccesslog());
		Log logError = new Log(xml.getErrorlog());

		ServerSocket server = new ServerSocket(xml.getPort());

		while (true) {
			Socket socket = server.accept();
			String ip = socket.getInetAddress().getHostAddress();

			if(new IpAddressMatcher(xml.getAccept()).matches(ip)){
				if(!new IpAddressMatcher(xml.getReject()).matches(ip)){
					System.out.println("connexion OK");
				}else {
					System.out.println("connexion refusée");
					socket.close();

				}
			}

			BufferedReader entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataOutputStream sortie = new DataOutputStream(socket.getOutputStream());
			try{
				String ligne = entree.readLine();
				String[] demande = ligne.split(" ");
				int i = 0;
				System.out.println((i++) + "\t" + ligne);
				while (!ligne.equals("")) {
					ligne = entree.readLine();
					System.out.println((i++) + "\t" + ligne);
				}
				if(demande[1].equals("/status")){

					String source = "<!DOCTYPE html><html><head><title>status</title></head><body><h1>status</h1><p>memoire non utilis&eacute;e : "+getMemoireNonUtilise()+"</p><p>disque disponible : "+getDisqueDisponible()+"</p><p>processus en cours : "+getProcessusEnCours()+"</p></body></html>";
					sortie.writeBytes("HTTP/1.1 200 OK\r\n");
					sortie.writeBytes("Content-Encoding: gzip\r\n");
					sortie.writeBytes("Content-Type: text/html\r\n");
					sortie.writeBytes("Content-Length: " + source.length() + "\r\n");
					sortie.writeBytes("\r\n");
					GZIPOutputStream gzip = new GZIPOutputStream(sortie);
					gzip.write(source.getBytes());
					gzip.close();

					sortie.flush();
				}
				File file = new File(xml.getRoot()+demande[1]);
				if(file.exists()){
					FileInputStream fichier = new FileInputStream(file);
					byte[] contenu = new byte[(int) file.length()];
					fichier.read(contenu);
					fichier.close();
					sortie.writeBytes("HTTP/1.1 200 OK\r\n");
					sortie.writeBytes("Content-Type: text/html\r\n");
					sortie.writeBytes("Content-Length: " + contenu.length + "\r\n");
					sortie.writeBytes("\r\n");
					sortie.write(contenu);
					sortie.flush();
					fichier.close();
					logAccess.ajouter(demande[1], ip, "200");
				} else{
					sortie.writeBytes("HTTP/1.1 404 Not Found\r\n");
					sortie.writeBytes("\r\n");
					logError.ajouter(demande[1], ip, "404");
				}
			}catch (Exception e){

			}
			socket.close();

		}
	}

	public static long getMemoireNonUtilise() {
		return Runtime.getRuntime().freeMemory();
	}

	public static long getDisqueDisponible() {
		return new File("/").getFreeSpace();
	}

	public static long getProcessusEnCours() {
		return Runtime.getRuntime().availableProcessors();
	}
}
