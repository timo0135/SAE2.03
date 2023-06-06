import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		int port = 8080;
		String root = null;
		String accept = null;
		String reject = null;
		String accesslog = null;
		String errorlog = null;

		File f = new File("config/webconf.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(f);
		document.getDocumentElement().normalize();
		System.out.println("Root Element :" + document.getDocumentElement().getNodeName());
		NodeList nList = document.getElementsByTagName("webconf");
		System.out.println("----------------------------");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			System.out.println("\nCurrent Element :" + nNode.getNodeName());
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				//System.out.println("webconfig : " + eElement.getAttribute("webconfig"));
				if(!eElement.getElementsByTagName("port").item(0).getTextContent().equals(""))
					port = Integer.parseInt(eElement.getElementsByTagName("port").item(0).getTextContent());
				root = eElement.getElementsByTagName("root").item(0).getTextContent();
				accept = eElement.getElementsByTagName("accept").item(0).getTextContent();
				reject = eElement.getElementsByTagName("reject").item(0).getTextContent();
				accesslog = eElement.getElementsByTagName("accesslog").item(0).getTextContent();
				errorlog = eElement.getElementsByTagName("errorlog").item(0).getTextContent();
			}
		}
		ServerSocket server = new ServerSocket(port);

		while (true) {
			Socket socket = server.accept();
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

				File file = new File(root+demande[1]);
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
