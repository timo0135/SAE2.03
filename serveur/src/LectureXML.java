import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LectureXML {
	// attributs
	private int port = 8080;
	private String root = "";
	private ArrayList<String> accept = new ArrayList<>();
	private ArrayList<String> reject = new ArrayList<>();
	private String accesslog = "";
	private String errorlog = "";

	public LectureXML(String chemin) throws ParserConfigurationException, IOException, SAXException {
		File f = new File(chemin);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(f);
		document.getDocumentElement().normalize();

		Element noeudPrincipal = (Element) document.getElementsByTagName("webconf").item(0);

		if (noeudPrincipal.getElementsByTagName("port").getLength() == 0)
			this.port = 80;
		else
			this.port = Integer.parseInt(noeudPrincipal.getElementsByTagName("port").item(0).getTextContent());

		if (noeudPrincipal.getElementsByTagName("root").getLength() == 0)
			this.root = "www";
		else
			this.root = noeudPrincipal.getElementsByTagName("root").item(0).getTextContent();

		for (int i = 0; i < noeudPrincipal.getElementsByTagName("accept").getLength(); i++)
			this.accept.add(noeudPrincipal.getElementsByTagName("accept").item(i).getTextContent());
		for (int i = 0; i < noeudPrincipal.getElementsByTagName("reject").getLength(); i++)
			this.reject.add(noeudPrincipal.getElementsByTagName("reject").item(i).getTextContent());

		if (noeudPrincipal.getElementsByTagName("accesslog").getLength() == 0)
			this.accesslog = "log/access.log";
		else
			this.accesslog = noeudPrincipal.getElementsByTagName("accesslog").item(0).getTextContent();

		if (noeudPrincipal.getElementsByTagName("errorlog").getLength() == 0)
			this.errorlog = "log/error.log";
		else
			this.errorlog = noeudPrincipal.getElementsByTagName("errorlog").item(0).getTextContent();
	}

	public int getPort() {
		return port;
	}

	public String getRoot() {
		return root;
	}

	public ArrayList<String> getAccept() {
		return accept;
	}

	public ArrayList<String> getReject() {
		return reject;
	}

	public String getAccesslog() {
		return accesslog;
	}

	public String getErrorlog() {
		return errorlog;
	}
}
