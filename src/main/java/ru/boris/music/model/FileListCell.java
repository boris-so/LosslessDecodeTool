package ru.boris.music.model;

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;

/**
 * ������� ����� ������ ������ � �������� �� ������.
 */
public class FileListCell extends ListCell<File>
{
    private boolean fullName;

    public FileListCell()
    {
        super();
        this.fullName = false;

        super.setOnMouseClicked(this::onMouseCLicked);
    }

    private void onMouseCLicked(MouseEvent me)
    {
        if (!me.getButton().equals(MouseButton.SECONDARY))
        {
            me.consume();
            return;
        }
        this.fullName = !this.fullName;
        this.updateItem(super.getItem(), super.isEmpty());
    }

    @Override protected void updateItem(File item, boolean empty)
    {
        super.updateItem(item, empty);
        if (empty || item == null)
        {
            super.setText(null);
            super.setGraphic(null);
        }
        else    // ����� ��� �� ��������� ����, � ����� ��� �����.
        {
            if (this.fullName) super.setText(item.toString());
            else super.setText(item.getName());
            super.setTooltip(new Tooltip(item.toString()));
        }
    }
}
