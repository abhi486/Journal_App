package model;

import com.google.firebase.Timestamp;

public class Journal {
    private String title;
    private String thought;
    private String imageURL;
    private String userId;
    private String username;
    private Timestamp TimeAdded;

    public Journal() {
    }

    public Journal(String title, String thought, String imageURL, String userId, String username, Timestamp timeAdded) {
        this.title = title;
        this.thought = thought;
        this.imageURL = imageURL;
        this.userId = userId;
        this.username = username;
        TimeAdded = timeAdded;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getTimeAdded() {
        return TimeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        TimeAdded = timeAdded;
    }
}
