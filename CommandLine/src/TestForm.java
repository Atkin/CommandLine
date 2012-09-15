import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.w3c.dom.Document;

//Главная форма десктоп приложения. Рисует интерфейс и вызывает команды
public class TestForm extends JFrame implements ActionListener, KeyListener {

	/** Инициализация элементов интерфейса */
	private JLabel lEnterCommands = new JLabel("Enter commands:");
	private JTextField tFEnterCommands = new JTextField();
	private JButton bSetData = new JButton("Ok");
	private String data;
	private JTextArea jTAdataOut = new JTextArea();
	private String commandName;
	private List<String> words = new ArrayList<String>();
	private Document doc = null;
	public String word = "";
	private SetDocument openDoc;
	private Class[] pluginClasses;

	/** Конструктор формы приложения */
	public TestForm(String s) {
		super(s);
		setLayout(null);
		Font f = new Font("Serif", Font.BOLD, 15);

		/** Расположение и параметры элементов интерфейса */
		this.lEnterCommands.setBounds(10, 260, 100, 30);
		add(this.lEnterCommands);

		this.tFEnterCommands.setBounds(120, 260, 200, 30);
		add(this.tFEnterCommands);
		this.tFEnterCommands.addKeyListener(this);
		this.tFEnterCommands.setCaretPosition(0);

		this.bSetData.setBounds(330, 260, 50, 30);
		add(this.bSetData);
		this.bSetData.addActionListener(this);

		this.jTAdataOut.setBounds(0, 0, 353, 240);
		add(this.jTAdataOut);
		JScrollPane jsp = new JScrollPane(this.jTAdataOut);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		jsp.setBounds(10, 10, 370, 240);
		add(jsp);
		setBounds(200, 100, 400, 335);
		
		/** Загружает плагины */
		/** Получает массив jar-файлов из папки plugins */
		File pluginDir = new File("plugins");
		File[] jars = pluginDir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".jar");
			}
		});

		/**
		 * Для каждого jar-файла из папки создает отдельный URLClassLoader и
		 * получаем объект типа Class по имени
		 */
		this.pluginClasses = new Class[jars.length];

		for (int i = 0; i < jars.length; i++) {

			try {
				Properties props = null;
				try {
					props = getPluginProps(jars[i]);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (props == null)
					throw new IllegalArgumentException("No props file found");

				String pluginClassName = props.getProperty("main.class");
				URL jarURL = jars[i].toURI().toURL();
				URLClassLoader classLoader = new URLClassLoader(
						new URL[] { jarURL });
				pluginClasses[i] = classLoader.loadClass(pluginClassName);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

	}

	/** Обработка события нажания на кнопку Ok */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand() == "Ok") {
			this.pluginStart();
		}
	}

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        	this.pluginStart();
        }
    }
	
	/** Обрабатывает поступающую команду, передает её в нужный плагин и запускает его */
	private void pluginStart() {
		this.word = "";

		/** Печать команды */
		this.jTAdataOut.append(this.tFEnterCommands.getText() + "\n");

		try {
			/** Разбивает команду на части */
			StringTokenizer token = new StringTokenizer(
					this.tFEnterCommands.getText());

			/** Определяем имя необходимой команды */
			this.commandName = token.nextElement().toString();

			/** Определяет параметры необходимой команды */
			for (int i = 1; i <= token.countTokens(); i++)
				this.word = this.word + token.nextElement().toString();

			/** Загружает xml-файл */
			if (this.commandName.equals("Open"))
				this.openDoc = new SetDocument(this.word);

			else {
				if (this.openDoc == null)
					this.jTAdataOut
							.append("Please input the correct:\n Open The_file_path\n");
				else {

					/**
					 * Поиск плагина, соответствующего необходимой команде и
					 * вызов его основных методов
					 */
					for (Class clazz : this.pluginClasses) {
						try {
							Plugin instance = (Plugin) clazz.newInstance();
							if (this.commandName.equals(instance
									.getCommandName())) {
								instance.getResult(
										this.openDoc.getDocument(),
										this.word);
								this.jTAdataOut.append(instance.getData()
										+ "\n");
							}
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}

		} catch (NoSuchElementException e) {
			this.jTAdataOut
					.append("Please input the correct command like this:\n CommandName CommandParameters\n");
		}
	}
	
	/** Загрузки информации о плагине из файла settings.properties */
	private Properties getPluginProps(File file) throws IOException {
		Properties result = null;
		JarFile jar = new JarFile(file);
		Enumeration<JarEntry> entries = jar.entries();

		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if (entry.getName().equals("settings.properties")) {
				InputStream is = null;
				try {
					is = jar.getInputStream(entry);
					result = new Properties();
					result.load(is);
				} finally {
					if (is != null)
						is.close();
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		TestForm frame = new TestForm("Test commands line");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

}
