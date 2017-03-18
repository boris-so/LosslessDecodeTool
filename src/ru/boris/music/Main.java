package ru.boris.music;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.boris.music.model.settings.Format;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

public class Main extends Application
{
    private static Stage mainStage;
    private static final String PROPS_FILE = "/program.properties";
    /**
     * Директория, в которой лежит jar-файл.
     */
    public static final String CUR_DIR;
    static
    {
        String dir;
        try { dir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent(); }
        catch (URISyntaxException ex)
        {
            ex.printStackTrace();
            System.out.println("Unable to get current dir location!");
            dir = System.getProperty("user.dir");
        }
        CUR_DIR = dir;
    }

    private static final Properties props = new Properties();


    // Загружаем настойки. Вызов будет перед start.
    @Override public void init() throws Exception
    {
        // debug: debug func here!
        this.debug();

        // И параметры программы
        try(InputStream is = this.getClass().getResourceAsStream(PROPS_FILE);
            InputStreamReader r = new InputStreamReader(is, Charset.forName("utf-8")))
        {
            props.load(r);
            System.out.println("Program properties were loaded.");

        }
        catch (IOException ex)
        {
            System.out.println("Some errors while trying to read properties:\n" + ex.toString());
            ex.printStackTrace();
        }
        catch (IllegalArgumentException ex)
        {
            System.out.println("Malformed Unicode escape appears in the input:\n" + ex.toString());
            ex.printStackTrace();
        }

        // Попробуем загрузить настройки декодеров
        // todo: загрузка базы
        try {  }
        catch (Exception ex) { ex.printStackTrace(); }

        System.out.println("Working dir: " + CUR_DIR);

    }

    @Override public void start(Stage primaryStage) throws Exception
    {
        Main.mainStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/ru/boris/music/view/MainWindow.fxml"));

        primaryStage.setTitle("Музыкальный декодер");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        Application.launch(args);
    }

    public static Stage getMainStage()
    {
        return Main.mainStage;
    }

    public static void showModalWindow(final URL fxml, final String title)
    {
        Parent root;
        try
        {
            root = FXMLLoader.load(fxml);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            Main.mainStage = stage;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        Main.mainStage.showAndWait();
    }


    // region Program properties
    synchronized public static String getDecodersFileName()
    {
        String prop = props.getProperty("decoders_serialize_file_name", "decoders.bin");
        return prop.startsWith("/") ? System.getProperty("user.dir") + prop : prop;
    }
    synchronized public static String getDecodedFileFormat()
    {
        return props.getProperty("decoded_file_format", "wav");
    }
    synchronized public static String getCuePref()
    {
        return props.getProperty("cue_prefix", "LDT_WAV_");
    }
    synchronized public static String getFileExtRegex()
    {
        return props.getProperty("file_ext_regex", "(?<=\\\\.)[A-Za-z0-9]{2,5}$");
    }
    // endregion

    // region Utils

    /**
     * Расширение файла после самой последней точки.
     * @param f    файл
     * @return null - если f == null или директория
     */
    public static String getExt(final File f)
    {
        if (f == null || f.isDirectory()) return null;

        final int dotId = f.getName().lastIndexOf('.');
        if (dotId == -1)    return "";
        else                return f.getName().substring(dotId + 1);
    }
    // endregion

    private void debug()
    {


        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("settings");
        final EntityManager em = emf.createEntityManager();


        em.getTransaction().begin();
        Format frm = new Format();
        frm.setFrm("ape");
        em.persist(frm);
        em.getTransaction().commit();


        if (true) return;
    }
}
