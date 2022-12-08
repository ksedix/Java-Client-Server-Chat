package message;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class for adding timestamp to messages as well as newline character necessary to display different messages on different lines
 */
public class Message implements Serializable {

    private final String message;

    /**
     * Prepends the current time to the string and stores it.
     * @param message given String
     */
    public Message(String message) {
        String time;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        time = "<" + dtf.format(now) + "> ";
        this.message = time + message+"\n";
    }

    /**
     * Returns the encapsulated String message
     * @return String object message
     */
    public String toString() {
        return message;
    }

    public String getMessagePart(){
        return message.substring(message.indexOf(">")+1);
    }

}