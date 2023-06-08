import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class Serveur {
	private LectureXML xml;
	private Log logAccess;
	private Log logError;
	private ServerSocket server;

	public Serveur() throws ParserConfigurationException, IOException, SAXException {
		this.xml = new LectureXML("config/webconf.xml");
		this.logAccess = new Log(xml.getAccesslog());
		this.logError = new Log(xml.getErrorlog());
		this.server = new ServerSocket(xml.getPort());
	}

	public void launch() throws IOException {
		while (true) {
			attendreEtExecuter();
		}
	}

	public boolean accepterIp(String ip) {
		for (String ipAccept : xml.getAccept())
			if (new IpAddressMatcher(ipAccept).matches(ip))
				return true;
		for (String ipReject : xml.getReject())
			if (new IpAddressMatcher(ipReject).matches(ip))
				return false;
		return xml.getAccept().size() == 0;
	}

	public void sendPage(byte[] page, DataOutputStream sortie) throws IOException {
		sortie.writeBytes("HTTP/1.1 200 OK\r\n");
		sortie.writeBytes("Content-Encoding: gzip\r\n");

		sortie.writeBytes("Content-Length: " + page.length + "\r\n");
		sortie.writeBytes("\r\n");
		GZIPOutputStream gzip = new GZIPOutputStream(sortie);
		gzip.write(page);
		gzip.close();
		sortie.flush();
	}

	public void attendreEtExecuter() throws IOException {
		System.out.println("\nEn attente d'une connexion...");
		Socket socket = server.accept();
		String ip = socket.getInetAddress().getHostAddress();
		System.out.println("Connexion acceptée : " + ip);


		BufferedReader entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		DataOutputStream sortie = new DataOutputStream(socket.getOutputStream());
		try {
			String ligne = entree.readLine();
			String[] demande = ligne.split(" ");

			System.out.println("Demande : " + demande[1]);

			// test d'autorisation de l'ip - erreur 403 si refuse
			if (!accepterIp(ip)) {
				sortie.writeBytes("HTTP/1.1 403 Forbidden\r\n");
				sortie.writeBytes("\r\n");
				logError.ajouter(demande[1], ip, "403");
				return;
			}

			/*
			int i = 0;
			System.out.println((i++) + "\t" + ligne);
			while (!ligne.equals("")) {
				ligne = entree.readLine();
				System.out.println((i++) + "\t" + ligne);
			}
			*/

			byte[] page;
			if (demande[1].equals("/status")) {
				// si demande de la page /status
				page = ("<!DOCTYPE html><html><head>" +
						"<title>Serveur statut</title>" +
						"</head><body>" +
						"<h1>Statut</h1>" +
						"<p>M&eacute;moire libre : " + getMemoireNonUtilise() + "</p>" +
						"<p>Espace disque libre : " + getDisqueDisponible() + "</p>" +
						"<p>Processus lanc&eacute;e : " + getProcessusEnCours() + "</p>" +
						"</body></html>").getBytes();
			} else {
				// si demande d'une page autre
				File file = new File(xml.getRoot() + demande[1]);
				if (file.exists()) {
					// chargement de la page de la demande si elle existe
					FileInputStream fichier = new FileInputStream(file);
					page = new byte[(int) file.length()];
					fichier.read(page);
					fichier.close();
				} else {
					// erreur 404 si la page n'existe pas
					sortie.writeBytes("HTTP/1.1 404 Not Found\r\n");
					sortie.writeBytes("\r\n");

					logError.ajouter(demande[1], ip, "404");
					socket.close();
					return;
				}
			}

			page = findCode(new String(page, "ISO-8859-1")).getBytes("ISO-8859-1");
			sendPage(page, sortie);
			logAccess.ajouter(demande[1], ip, "200");
		} catch (Exception e) {
			e.printStackTrace();
		}
		socket.close();

	}

	public String findCode(String page) {
		Pattern pattern = Pattern.compile("<code interpreteur=\"(.*?)\">(.*?)</code>");
		Matcher matcher = pattern.matcher(page);
		String resultat = page;
		while (matcher.find()) {
			String code = matcher.group();
			String interpreteur = code.substring(code.indexOf("\"") + 1, code.lastIndexOf("\""));
			String codeCoupe = code.substring(code.indexOf(">") + 1, code.lastIndexOf("<"));
			String codeResult = executeCode(interpreteur, codeCoupe);
			resultat = resultat.replaceAll(Pattern.quote(code), codeResult);
		}
		return resultat;
	}

	public String executeCode(String interpreteur, String code) {
		try {
			Process process = Runtime.getRuntime().exec(interpreteur);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
			String result = "";
			for (String ligneCode : code.split("\n")) {
				writer.println(ligneCode);
				writer.close();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
				String ligne;
				while ((ligne = reader.readLine()) != null) {
					result += ligne + "\n";
				}
				reader.close();
			}
			return result;
		} catch (IOException e) {
			System.out.println("Erreur d'execution du code : " + code);
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
		try {
			Serveur serveur = new Serveur();
			serveur.launch();
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
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
