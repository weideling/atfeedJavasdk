package stockanalysis.io;

import at.feedapi.ActiveTickServerAPI;
import at.feedapi.Helpers;
import at.shared.ATServerAPIDefines;
import at.shared.ATServerAPIDefines.ATBarHistoryType;
import at.shared.ATServerAPIDefines.ATGUID;
import at.shared.ATServerAPIDefines.ATSYMBOL;
import at.shared.ATServerAPIDefines.SYSTEMTIME;
import at.utils.jlib.Errors;
import com.google.common.base.Preconditions;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static at.feedapi.ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT;
import static at.shared.ATServerAPIDefines.ATBarHistoryType.BarHistoryIntraday;

public class DataDownloader {

    // let's hardcoded endpoint and credentials etc

    private static final String API_KEY = "don't check in";

    private static final ATGUID AT_GUID = createAtguid();

    private static final String HOST_NAME = "activetick1.activetick.com";

    private static final int PORT = 443;

    private static final String USER = "xxh518";

    private static final String PASSWORD = "pennstate1";

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss").withZone(DateTimeZone.forID("America/New_York"));

    private static final short DATA_PERIOD_IN_MIN = 1;

    private final ActiveTickServerAPI server = new ActiveTickServerAPI();

    private final APISession apiSession = new APISession(server);

    private DataDownloader() {}

    public static DataDownloader create() {
        return new DataDownloader();
    }

    public static DateTimeFormatter formatter() {
        return FORMATTER;
    }

    private static ATGUID createAtguid() {
        ATGUID atguid = (new ATServerAPIDefines()).new ATGUID();
        Preconditions.checkState(atguid.SetGuid(API_KEY), "failed to set guid");
        return atguid;
    }

    public void initAndLogin() {
        server.ATInitAPI();
        Preconditions.checkState(apiSession.Init(AT_GUID, HOST_NAME, PORT, USER, PASSWORD), "failed to log in");
    }

    public void shutdown() {
        apiSession.UnInit();
        server.ATShutdownAPI();
    }

    public void getIntradayHistoryBars(String symbol,
                                       ReadableInstant start,
                                       ReadableInstant end,
                                       PrintStream output) throws InterruptedException {
        ATSYMBOL stock = Helpers.StringToSymbol(symbol);
        SYSTEMTIME from = Helpers.StringToATTime(FORMATTER.print(start));
        SYSTEMTIME to = Helpers.StringToATTime(FORMATTER.print(end));

        ATBarHistoryType barHistoryType = (new ATServerAPIDefines()).new ATBarHistoryType(BarHistoryIntraday);
        CountDownLatch done = new CountDownLatch(1);
        Requestor requestor = apiSession.GetRequestor();
        requestor.setOutput(output);
        requestor.setRequestDone(done);
        long request = apiSession.GetRequestor().SendATBarHistoryDbRequest(stock, barHistoryType,
                DATA_PERIOD_IN_MIN, from, to, DEFAULT_REQUEST_TIMEOUT);
        Preconditions.checkState(request >= 0, "request failed: " + Errors.GetStringFromError((int)request));
        done.await();
    }
}
