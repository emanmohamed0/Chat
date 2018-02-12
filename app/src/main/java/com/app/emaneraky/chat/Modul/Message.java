package com.app.emaneraky.chat.Modul;

public class Message {
    private String message, type, from;
    private Long time;
    private boolean seen;

    public Message() {
    }

    public Message(boolean seen, Long time, String message, String type, String from) {
        this.seen = seen;
        this.time = time;
        this.message = message;
        this.type = type;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
