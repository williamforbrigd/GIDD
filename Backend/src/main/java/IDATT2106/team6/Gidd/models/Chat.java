package IDATT2106.team6.Gidd.models;

import javax.persistence.*;

@Entity
public class Chat {
    @Id
    @Column(name = "chat_id", unique = true)
    private Integer chatId;
    @JoinColumn(name = "group_id")
    @ManyToOne(targetEntity = Group.class)
    private Group group;
    @JoinColumn(name = "user_sent_id")
    @ManyToOne(targetEntity = User.class)
    private User user;
    private String message;

    public Chat(Group group, User user, String message){
        this.group = group;
        this.user = user;
        this.message = message;
    }

    public Chat(){}

    public Integer getChatId() {
        return chatId;
    }

    public Group getGroup() {
        return group;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}