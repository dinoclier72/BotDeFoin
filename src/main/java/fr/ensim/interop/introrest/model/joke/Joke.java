package fr.ensim.interop.introrest.model.joke;

public class Joke {

    private static int ROULEAU = 0;

    private Integer id;

    private float grade;
    private String title;
    private String content;

    private String category;
    public Joke(){}
    public Joke(float grade, String title, String content, String category) {
        this.id = ROULEAU++;
        this.grade = grade;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void generateJokeId(){
        this.id = ROULEAU++;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public float getGrade() {
        return grade;
    }

    public void setGrade(float grade) {
        this.grade = grade;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {return content;}

    public void setContent(String content) {this.content = content;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}
}
