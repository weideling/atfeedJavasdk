package stockanalysis.io;

import at.feedapi.ATCallback;
import at.feedapi.ActiveTickServerAPI;
import at.feedapi.Helpers;
import at.feedapi.Session;
import at.feedapi.ATCallback.ATLoginResponseCallback;
import at.feedapi.ATCallback.ATRequestTimeoutCallback;
import at.feedapi.ATCallback.ATServerTimeUpdateCallback;
import at.feedapi.ATCallback.ATSessionStatusChangeCallback;
import at.feedapi.ATCallback.ATOutputMessageCallback;
import at.utils.jlib.Errors;
import at.utils.jlib.OutputMessage;
import at.shared.ATServerAPIDefines;
import at.shared.ATServerAPIDefines.ATGUID;
import at.shared.ATServerAPIDefines.ATLOGIN_RESPONSE;
import at.shared.ATServerAPIDefines.SYSTEMTIME;

public class APISession extends ATCallback implements
        ATLoginResponseCallback, ATServerTimeUpdateCallback, ATRequestTimeoutCallback, ATSessionStatusChangeCallback, ATOutputMessageCallback {

    private Session session;
    private ActiveTickServerAPI server;
    private Requestor requestor;
    private Streamer streamer;

    private long lastRequest;
    private String userid;
    private String password;
    private ATGUID apiKey;

    public APISession(ActiveTickServerAPI serverapi) {
        server = serverapi;
    }

    public ActiveTickServerAPI GetServerAPI() {
        return server;
    }

    public Session GetSession() {
        return session;
    }

    public Streamer GetStreamer() {
        return streamer;
    }

    public Requestor GetRequestor() {
        return requestor;
    }

    public boolean Init(ATGUID apiKey, String serverHostname, int serverPort, String userId, String password) {
        if (session != null)
            server.ATShutdownSession(session);

        session = server.ATCreateSession();
        streamer = new Streamer(this);
        requestor = new Requestor(server, session, streamer);

        userid = userId;
        this.password = password;
        this.apiKey = apiKey;

        long rc = server.ATSetAPIKey(session, this.apiKey);

        session.SetServerTimeUpdateCallback(this);
        session.SetOutputMessageCallback(this);

        boolean initrc = false;
        if (rc == Errors.ERROR_SUCCESS)
            initrc = server.ATInitSession(session, serverHostname, serverHostname, serverPort, this);

        System.out.println(server.GetAPIVersionInformation());
        System.out.println("--------------------------------------------------------------------");

        return initrc;
    }

    public boolean UnInit() {
        if (session != null) {
            server.ATShutdownSession(session);
            session = null;
        }

        return true;
    }

    //ATLoginResponseCallback
    public void process(Session session, long requestId, ATLOGIN_RESPONSE response) {
        String strLoginResponseType = "";
        switch (response.loginResponse.m_atLoginResponseType) {
            case ATServerAPIDefines.ATLoginResponseType.LoginResponseSuccess:
                strLoginResponseType = "LoginResponseSuccess";
                break;
            case ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidUserid:
                strLoginResponseType = "LoginResponseInvalidUserid";
                break;
            case ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidPassword:
                strLoginResponseType = "LoginResponseInvalidPassword";
                break;
            case ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidRequest:
                strLoginResponseType = "LoginResponseInvalidRequest";
                break;
            case ATServerAPIDefines.ATLoginResponseType.LoginResponseLoginDenied:
                strLoginResponseType = "LoginResponseLoginDenied";
                break;
            case ATServerAPIDefines.ATLoginResponseType.LoginResponseServerError:
                strLoginResponseType = "LoginResponseServerError";
                break;
            default:
                strLoginResponseType = "unknown";
                break;
        }

        System.out.println("RECV " + requestId + ": Login Response [" + strLoginResponseType + "]");
    }

    //ATServerTimeUpdateCallback
    public void process(SYSTEMTIME serverTime) {
    }

    //ATRequestTimeoutCallback
    public void process(long origRequest) {
        System.out.println("(" + origRequest + "): Request timed-out\n");
    }

    //ATSessionStatusChangeCallback
    public void process(Session session, ATServerAPIDefines.ATSessionStatusType type) {
        String strStatusType = "";
        switch (type.m_atSessionStatusType) {
            case ATServerAPIDefines.ATSessionStatusType.SessionStatusConnected:
                strStatusType = "SessionStatusConnected";
                break;
            case ATServerAPIDefines.ATSessionStatusType.SessionStatusDisconnected:
                strStatusType = "SessionStatusDisconnected";
                break;
            case ATServerAPIDefines.ATSessionStatusType.SessionStatusDisconnectedDuplicateLogin:
                strStatusType = "SessionStatusDisconnectedDuplicateLogin";
                break;
            default:
                break;
        }

        System.out.println("RECV Status change [" + strStatusType + "]");

        //if we are connected to the server, send a login request
        if (type.m_atSessionStatusType == ATServerAPIDefines.ATSessionStatusType.SessionStatusConnected) {
            lastRequest = server.ATCreateLoginRequest(session, userid, password, this);
            boolean rc = server.ATSendRequest(session, lastRequest, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT, this);

            System.out.println("SEND (" + lastRequest + "): Login request [" + userid + "] (rc = " + (char) Helpers.ConvertBooleanToByte(rc) + ")");
        }
    }

    public void process(OutputMessage outputMessage) {
        System.out.println(outputMessage.GetMessage());
    }
}
