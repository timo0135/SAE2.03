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

public class LectureXML {
	// attributs
	private int port = 8080;
	private String root = "";
	private String accept = "";
	private String reject = "";
	private String accesslog = "";
	private String errorlog = "";

	public LectureXML(String chemin) throws ParserConfigurationException, IOException, SAXException {

		File f = new File(chemin);
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
					this.port = Integer.parseInt(eElement.getElementsByTagName("port").item(0).getTextContent());
				this.root = eElement.getElementsByTagName("root").item(0).getTextContent();
				this.accept = eElement.getElementsByTagName("accept").item(0).getTextContent();
				this.reject = eElement.getElementsByTagName("reject").item(0).getTextContent();
				this.accesslog = eElement.getElementsByTagName("accesslog").item(0).getTextContent();
				this.errorlog = eElement.getElementsByTagName("errorlog").item(0).getTextContent();

			}
		}
	}

	public int getPort() {
		return port;
	}

	public String getRoot() {
		return root;
	}

	public String getAccept() {
		return accept;
	}

	public String getReject() {
		return reject;
	}

	public String getAccesslog() {
		return accesslog;
	}

	public String getErrorlog() {
		return errorlog;
	}
}
