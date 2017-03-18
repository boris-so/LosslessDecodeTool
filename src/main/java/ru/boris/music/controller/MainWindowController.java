package ru.boris.music.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import ru.boris.music.Main;
import ru.boris.music.model.FileListCell;
import ru.boris.music.model.NewCueTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable
{
    /**
     * Окно с журналом декодирования
     */
    private static final Stage stLog;
    private static final TextArea taLogFld;
    private static Border srcBrg;
    private static final Tooltip PUP;

    static
    {
        stLog = new Stage();
        stLog.setTitle("Журнал");
        AnchorPane root = new AnchorPane();
        Scene sc = new Scene(root, 500, 700);
        stLog.setScene(sc);
        stLog.initModality(Modality.NONE);
        stLog.initOwner(Main.getMainStage());

        taLogFld = new TextArea();
        taLogFld.setEditable(false);
        taLogFld.textProperty().addListener(observable -> taLogFld.setScrollTop(Double.MAX_VALUE));
        root.getChildren().add(taLogFld);
        AnchorPane.setTopAnchor(taLogFld, 10d);
        AnchorPane.setBottomAnchor(taLogFld, 10d);
        AnchorPane.setLeftAnchor(taLogFld, 10d);
        AnchorPane.setRightAnchor(taLogFld, 10d);
        stLog.setX(50); stLog.setY(50);
        Tooltip.install(taLogFld, new Tooltip("Press del to clear"));
        taLogFld.setOnKeyPressed(e ->
        {
            switch (e.getCode())
            {
                case DELETE: taLogFld.clear(); break;
                case ESCAPE: stLog.close();
            }
        });

        PUP = new Tooltip();
        PUP.setAutoHide(true);
        PUP.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_RIGHT);
        PUP.setOpacity(0.6);
    }


    // region FXML
    /**
     * Заполнение идёт через множество. Если не будет других источников данных - тут будут только уникальные файлы.
     */
    @FXML private ListView<File> lwFiles;
    @FXML private MenuItem miAddFiles, miSettings, miOpenLog;
    @FXML private CheckMenuItem cmiShowLog, cmiMakeNewCue, cmiRemoveFiles;
    @FXML private Button btnDecode;
    @FXML private ProgressIndicator piIndicator;
    @FXML private ProgressBar pbProgress;
    // endregion

    private final DirectoryChooser dchDlg;
    private String lastText;        // Чтобы при декодировании перезаписывалась строчка с прогрессом в TextArea


    public MainWindowController()
    {
        this.dchDlg = new DirectoryChooser();
        this.dchDlg.setTitle("Выберите папку, в которой следует искать...");

        this.lastText = "";
    }


    @Override public void initialize(URL location, ResourceBundle resources)
    {
        this.miAddFiles.setOnAction(this::miAddFiles_onAction);
        this.miSettings.setOnAction(e -> Main.showModalWindow(
                this.getClass().getResource("/ru/boris/ru.boris.music/view/Settings.fxml"), "Настройки"));
        this.miOpenLog.setOnAction(e -> MainWindowController.stLog.show());
        this.btnDecode.setOnAction(this::btnDecode_onAction);
        this.cmiMakeNewCue.setOnAction(ae ->
        {
            PUP.setText(this.cmiMakeNewCue.isSelected() ?
                    "CUE-файлы будут исправлены" :
                    "CUE-файлы исправлять не будем");
            PUP.show(Main.getMainStage());
        });
        this.cmiRemoveFiles.setOnAction(ae ->
        {
            PUP.setText(this.cmiRemoveFiles.isSelected() ?
                    "Исходные файлы будут удалены" :
                    "Исходные файлы удаляться не будут");
            PUP.show(Main.getMainStage());
        });
        this.cmiShowLog.setOnAction(ae ->
        {
            PUP.setText(this.cmiShowLog.isSelected() ?
                    "После старта будет показан журнал" :
                    "Не открывать журнал после старта");
            PUP.show(Main.getMainStage());
        });

        // region Настройки ListView
        this.lwFiles.setItems(FXCollections.observableArrayList(FXCollections.emptyObservableSet()));
        this.lwFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.lwFiles.setOnKeyPressed(this::lwFiles_onKeyPressed);
        this.lwFiles.setOnDragDropped(this::lwFiles_onDragDropped);
        this.lwFiles.setOnDragOver(this::lwFiles_onDragOver);
        this.lwFiles.setOnDragExited(e -> this.lwFiles.setBorder(MainWindowController.srcBrg));
        MainWindowController.srcBrg = this.lwFiles.getBorder();
        Tooltip.install(this.lwFiles, new Tooltip("Перетащите сюда файлы"));
        this.lwFiles.setCellFactory(lv -> new FileListCell());
        // endregion

        this.cmiShowLog.setSelected(true);
    }


    private void lwFiles_onDragOver(DragEvent e)
    {
        if (e.getDragboard().hasFiles())
        {
            this.lwFiles.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID,
                    new CornerRadii(5), new BorderWidths(2))));
            e.acceptTransferModes(TransferMode.LINK);
        }
        else
            e.consume();
    }

    private void lwFiles_onDragDropped(DragEvent e)
    {
        if (e.getDragboard().hasFiles()) this.appendFiles(e.getDragboard().getFiles());

        e.setDropCompleted(e.getDragboard().hasFiles());
        e.consume();
    }

    private void miAddFiles_onAction(ActionEvent e)
    {
        // debug: this
        final Path dir = Paths.get(this.dchDlg.showDialog(Main.getMainStage()).toURI());
        try
        {
            ArrayList<File> files = new ArrayList<>();
            Files.walk(dir).forEach(f -> files.add(f.toFile()));
            this.appendFiles(files);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            final Alert al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("Ошибка");
            al.setHeaderText("Не удалось собрать файлы!");
            al.setContentText(ex.toString());
            al.showAndWait();
        }


    }

    private void lwFiles_onKeyPressed(KeyEvent e)
    {
        switch(e.getCode())
        {
            case DELETE:
                final int selId = this.lwFiles.getSelectionModel().getSelectedIndex();
                // Нужно оформлять отдельный массив, потому что при удалении из getItems()
                // объект удаляется из getSelectedItems() (наверное коллекция там одна, просто фильтруется)
                File[] selected = this.lwFiles.getSelectionModel().getSelectedItems().toArray(
                        new File[this.lwFiles.getSelectionModel().getSelectedItems().size()]);
                for (int i = 0; i < selected.length; i++)
                {
                    this.lwFiles.getItems().remove(selected[i]);
                    selected[i] = null;
                }

                this.lwFiles.getSelectionModel().clearSelection();
                if (this.lwFiles.getItems().size() < selId)
                    this.lwFiles.getSelectionModel().select(this.lwFiles.getItems().size() - 1);
                else
                    this.lwFiles.getSelectionModel().select(selId);
                break;
        }
    }

    private void btnDecode_onAction(ActionEvent e)
    {
        // region Сообщение, если файлы не выбраны
        if (this.lwFiles.getItems().size() == 0)
        {
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setTitle("Warning");
            al.setHeaderText("No files.");
            al.showAndWait();
            return;
        }
        // endregion

        // Настройка отображения...
        this.btnDecode.setDisable(true);
        MainWindowController.taLogFld.appendText("\n\nStarted at " + LocalDateTime.now().toString() + "...\n");
        this.piIndicator.setProgress(-1);       // Запустим бегающий кружок
        this.piIndicator.setVisible(true);
        if (this.cmiShowLog.isSelected()) MainWindowController.stLog.show();
        this.lwFiles.setDisable(true);

        // todo: тут должен быть запуск всей процедуры

        // region Запуск CUE-редактора в фоновом потоке
        // todo: проверить работу cue-редактора, наверное его тоже придётся переделать.
        if (!cmiMakeNewCue.isSelected()) return;

        MainWindowController.taLogFld.appendText("Getting parent folder to create new cue-sheets...\n");
        HashSet<File> dirs = new HashSet<>();
        for (File f : this.lwFiles.getItems()) dirs.add(f.getParentFile());
        NewCueTask newCue = new NewCueTask(dirs, cmiRemoveFiles.isSelected());
        newCue.messageProperty().addListener((observable, oldValue, newValue) -> taLogFld.appendText(newValue + "\n"));

        Thread cuer = new Thread(newCue);
        cuer.setName("CueEditor");
        newCue.setOnSucceeded(ev ->
        {
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("Info");
            al.setHeaderText(cuer.getName());
            al.setContentText("Cue sheets were modified successfully" +
                    ((this.cmiRemoveFiles.isSelected() ? " with " : " without ") + "deleting src files."));
            al.show();
        });
        cuer.setDaemon(true);
        cuer.start();
        // endregion
    }

    private void onDecodeFinished()
    {
        this.pbProgress.progressProperty().unbind();
        taLogFld.appendText("Done at: " + LocalDateTime.now());
        this.piIndicator.setVisible(false);
        this.btnDecode.setDisable(false);
        this.lwFiles.setDisable(false);

        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle("Info");
        al.setHeaderText("Decoder");
        al.setContentText("Files were decoded successfully" +
                ((this.cmiRemoveFiles.isSelected() ? " with " : " without ") + "deleting src files."));
        al.show();
    }

    /**
     * Функция для добавления новых файлов в список. Добавляются только те, которых ещё нет.
     * @param newFiles    список новых файлов на добавление
     */
    private void appendFiles(final List<File> newFiles)
    {
        if (newFiles == null || newFiles.isEmpty()) return;

        // Добавлять будем только те файлы, которых ещё нет.
        final ObservableList<File> files = this.lwFiles.getItems();
        for (File f : newFiles) if (!files.contains(f)) files.add(f);

        // Отсортируем файлы по расширениям
        files.sort((o1, o2) -> Main.getExt(o1).compareToIgnoreCase(Main.getExt(o2)));

    }
}
