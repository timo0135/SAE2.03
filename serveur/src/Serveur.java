import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// Lecture fichier xml
		LectureXML xml = new LectureXML("config/webconf.xml");

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
				} else{
					sortie.writeBytes("HTTP/1.1 404 Not Found\r\n");
					sortie.writeBytes("\r\n");
				}
			}catch (Exception e){

			}
			socket.close();


		}


	}

}
