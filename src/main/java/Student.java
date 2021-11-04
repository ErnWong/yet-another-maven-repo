import javax.swing.text.html.HTMLDocument;
import java.awt.*;

public class Student extends Person {
    public final HTMLDocument testScore;

    public Student(String firstName, String lastName, int age, Color eyeColour, Color hairColour, HTMLDocument testScore) {
        super(firstName, lastName, age, eyeColour, hairColour);
        this.testScore = testScore;
    }

    public HTMLDocument getTestScore() {
        return this.testScore;
    }
}

