package ru.boris.music.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable
{
    @FXML private Button btnSave, btnClose;
    @FXML private ListView lwFormats, lwOperations;
    @FXML private TextArea taComment;
    @FXML private MenuItem miAddFrm, miDelFrm, miAddOper, miDelOper;

    @Override public void initialize(URL location, ResourceBundle resources)
    {

    }
}