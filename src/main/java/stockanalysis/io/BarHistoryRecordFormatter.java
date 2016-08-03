package stockanalysis.io;

import at.shared.ATServerAPIDefines;
import at.utils.jlib.PrintfFormat;

/**
 * Serialize and deserialize {@link at.shared.ATServerAPIDefines.ATBARHISTORY_RECORD}
 */
public final class BarHistoryRecordFormatter {

    public static ATServerAPIDefines.ATBARHISTORY_RECORD parse(String line) {
        throw new UnsupportedOperationException("Needs impl");
    }

    public static String print(ATServerAPIDefines.ATBARHISTORY_RECORD record) {

        StringBuilder sb = new StringBuilder();
        String strFormat = "%02d";
        sb.append("[").append(String.format(strFormat, record.barTime.month)).append("/").append(String.format(strFormat, record.barTime.day)).append("/");
        strFormat = "%4d";
        sb.append(String.format(strFormat, record.barTime.year)).append(" ");
        strFormat = "%02d";
        sb.append(String.format(strFormat, record.barTime.hour))
                .append(":")
                .append(String.format(strFormat, record.barTime.minute))
                .append(":")
                .append(String.format(strFormat, record.barTime.second))
                .append("], ");

        strFormat = "%0." + record.open.precision + "f";
        sb.append("[o:").append(new PrintfFormat(strFormat).sprintf(record.open.price)).append(", ");

        strFormat = "%0." + record.high.precision + "f";
        sb.append("h:").append(new PrintfFormat(strFormat).sprintf(record.high.price)).append(", ");

        strFormat = "%0." + record.low.precision + "f";
        sb.append("l:").append(new PrintfFormat(strFormat).sprintf(record.low.price)).append(", ");

        strFormat = "%0." + record.close.precision + "f";
        sb.append("c:").append(new PrintfFormat(strFormat).sprintf(record.close.price)).append(", ");

        sb.append("vol:").append(record.volume);

        sb.append(']');

        return sb.toString();
    }

    private BarHistoryRecordFormatter() {}
}
