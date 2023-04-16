package fr.ensim.interop.introrest.jokemodel;

public class Joke {

    private Integer id;

    private float grade;

    private String question;

    private String answer;

    private String category;

    public Joke() {
    }

    public Joke(float grade, String question, String answer, String category) {
        this.grade = grade;
        this.question = question;
        this.answer = answer;
        this.category = category;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
