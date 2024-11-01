package org.example.up_itog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer score;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_student"))
    private Client student;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "course_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_course"))
    private Course course;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Client getStudent() {
        return student;
    }

    public void setStudent(Client student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
