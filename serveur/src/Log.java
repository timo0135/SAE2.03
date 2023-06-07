import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private PrintWriter writer;

	public Log(String source) throws IOException {
		this.writer = new PrintWriter(new FileWriter(source, true));
	}

	public void ajouter(String page, String ip, String status) {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String message = dateFormat.format(date) + " " + page + " " + ip + " " + status;

		this.writer.println(message);
		this.writer.flush();
	}

	public void close() {
		this.writer.close();
	}
}
