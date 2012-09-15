import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Класс, реализующий поиск количества заданных элементов в xml-файле */
public class Find implements Plugin {

	private int countElements = 0;
	private String commandName = "Find";
	private String findName = "";

	/** Возвращает имя команды */
	public String getCommandName() {
		return this.commandName;
	}

	/** Возвращает подсчитанное количество заданных элементов */
	public String getData() {
		return "Count Elements named \"" + this.findName + "\" = "
				+ Integer.toString(this.countElements);
	}

	/** Производит поиск заданных элементов элементы по xml-файлу */
	public void getResult(Node node, String word) {
		int type = node.getNodeType();
		this.findName = word;

		switch (type) {
		case Node.DOCUMENT_NODE: {
			getResult(((Document) node).getDocumentElement(), this.findName);
			break;
		}
		case Node.ELEMENT_NODE: {
			if (node.getNodeName().equals(this.findName))
				this.countElements++;

			NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++)
				getResult(attrs.item(i), this.findName);

			if (node.hasChildNodes()) {
				NodeList children = node.getChildNodes();
				for (int i = 0; i < children.getLength(); i++)
					getResult(children.item(i), this.findName);
			}
			break;
		}
		}
	}
}