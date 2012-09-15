import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/** Получает xml-документ по входящему адресу */
public class SetDocument {

	private File xmlFile;
	private Document doc;

	public SetDocument(String file) {
		this.xmlFile = new File(file);
		this.doc = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(this.xmlFile);
		} catch (Exception e) {
			System.err.println("Sorry, an error occurred: " + e);
		}
	}

	/** Возвращает полученный документ для дальнейшей обработки */
	public Document getDocument() {
		return this.doc;
	}
}
