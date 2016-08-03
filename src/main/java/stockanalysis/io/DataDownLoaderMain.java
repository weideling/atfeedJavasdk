package stockanalysis.io;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Entry point.
 */
public class DataDownLoaderMain {
    public static void main(String[] argv) throws Exception {
        DataDownloader downloader = DataDownloader.create();
        String stock = "CSCO";
        DateTime start = new DateTime(2010, 1, 1, 0, 0); // midnight of 2010/01/01
        Duration duration = Duration.standardDays(30);

        downloader.initAndLogin();
        Thread.sleep(1000L);

        Path dir = Paths.get("/tmp/stock/" + stock);
        Files.createDirectories(dir);
        boolean done = false;
        while (!done) {
            DateTime end = start.plus(duration);
            if (end.isAfterNow()) {
                done = true;
                end = DateTime.now().minus(Duration.standardMinutes(1));
            }
            Path filePath = Paths.get(dir.toString(),
                    String.format("%s-%s.csv",
                            DataDownloader.formatter().print(start),
                            DataDownloader.formatter().print(end)));
            Files.deleteIfExists(filePath);
            File file = Files.createFile(filePath).toFile();

            try (PrintStream output = new PrintStream(file)) {
                downloader.getIntradayHistoryBars(stock, start, end, output);
            }
            System.out.println("Finish at " + end);
            start = end;
        }
    }
}
