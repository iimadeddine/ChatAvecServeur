
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/serverChat")
public class ServerChat {
    static Set<Session> users = Collections.synchronizedSet(new HashSet<Session>());
    @OnOpen
    public void handleOpen(Session userSession) {
        System.out.println(userSession);
        users.add(userSession);
    }
    @OnMessage
    public String handleMessage(String message, Session userSession) throws IOException {
        String user = (String) userSession.getUserProperties().get("username");
        if (user == null) {
            userSession.getUserProperties().put("username", message);
            userSession.getBasicRemote().sendText(buildJsonData("System", "you are now connected as " + message));
        } else {
            for (Session session : users) {
                session.getBasicRemote().sendText(buildJsonData(user, message));
            }
        }
        return null;
    }
    @OnClose
    public void handleClose(Session userSession) {
        users.remove(userSession);
        System.out.println("client is disconnected...");
    }
    private String buildJsonData(String user, String message) {
        JsonObject jo = Json.createObjectBuilder().add("message", user + ":" + message).add("user", users.size()).build();
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(stringWriter);
        jsonWriter.write(jo);
        return stringWriter.toString();
    }
}
