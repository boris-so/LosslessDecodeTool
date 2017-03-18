package ru.boris.music.model;

import javafx.concurrent.Task;
import ru.boris.music.Main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NewCueTask extends Task<Void>
{
    private final ArrayList<Path> cueFiles;
    private final boolean removeSrcFiles;

    @SuppressWarnings("ConstantConditions") public NewCueTask(final HashSet<File> dirs, boolean removeFiles)
    {
        super();
        this.cueFiles = new ArrayList<>();
        this.removeSrcFiles = removeFiles;

        if (dirs == null || dirs.isEmpty()) return;
        // Вложенность папок - 1 уровень. По дереву ходить не будем - пустое это.
        // Заполним cue-файлами коллекцию из входного массива родительских каталогов
        for (File d : dirs) if (d != null && d.isDirectory() && d.exists())
        {
            for (File f : d.listFiles())
                if (f != null && f.isFile() && f.getName().toLowerCase().endsWith(".cue") &&
                        !f.getName().toUpperCase().startsWith(Main.getCuePref()))    // Свои же повторно не будем обрабатывать
                    this.cueFiles.add(Paths.get(f.toURI()));

        }
    }

    @Override protected Void call() throws Exception
    {
        for (Path cue : this.cueFiles) try
        {
            super.updateMessage(String.format("Looking up \'%s\'", cue));

            List<String> lines = Files.readAllLines(cue);
            for (int i = 0; i < lines.size(); i++)
            {
                String l = lines.get(i);
                String cueFileName;
                if (l.toUpperCase().startsWith("FILE"))     // Значит что в строке записано имя файла
                {
                    // Из всей строки выделим только имя файла
                    if (l.contains("\""))
                        // Если есть кавычка - имя файла внутри кавычек.
                        cueFileName = l.substring(l.indexOf("\"")+1, l.indexOf("\"", 6));
                    else
                        // если нет - имя файла между пробелами
                        cueFileName = l.substring(l.indexOf(" ")+1, l.indexOf(" ", 5));

                    // Дальше проверим расширение на wav
                    if (cueFileName.toLowerCase().endsWith(".wav"))
                        // Добавим к имени файла respl
                        l = String.format("FILE \"respl_%s\" WAVE", cueFileName);
                    else
                        // Заменим расширение на wav
                        l = String.format("FILE \"%s\" WAVE", cueFileName.replaceAll(Main.getFileExtRegex(), "wav"));
                }

                lines.set(i, l);
            }

            String newCue = Main.getCuePref() + cue.getFileName().toString();
            Files.write(Paths.get(cue.getParent().toString(), newCue), lines, Charset.forName("utf-8"));
            super.updateMessage("New CUE was created successfully: " + newCue);

            if (this.removeSrcFiles)
            {
                Files.delete(cue);
                super.updateMessage("Original CUE was removed");
            }
        }
        catch (IOException | SecurityException | UnsupportedOperationException ex)
        {
            super.updateMessage("\n" + ex.toString());
        }

        return null;
    }
}
