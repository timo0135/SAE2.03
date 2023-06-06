import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Serveur {

	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(args.length > 0 ? Integer.parseInt(args[0]) : 8080);
		String root = "www";

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
