package me.itangqi.greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table NOTE.
 */
public class Note {

    private Long id;
    /** Not-null value. */
    private String Title;
    private String comment;
    private java.util.Date date;
    private String diary;
    private String map;

    public Note() {
    }

    public Note(Long id) {
        this.id = id;
    }

    public Note(Long id, String Title, String comment, java.util.Date date, String diary, String map) {
        this.id = id;
        this.Title = Title;
        this.comment = comment;
        this.date = date;
        this.diary = diary;
        this.map = map;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getTitle() {
        return Title;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public String getDiary() {
        return diary;
    }

    public void setDiary(String diary) {
        this.diary = diary;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

}
