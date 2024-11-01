package org.example.up_itog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REFRESH, orphanRemoval = true)
    private Set<Grade> grades;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REFRESH, orphanRemoval = true)
    private Set<Schedule> schedules;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REFRESH, orphanRemoval = true)
    private Set<ClientCourse> clientCourses;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Set<Grade> getGrades() {
        return grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }

    public Set<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(Set<Schedule> schedules) {
        this.schedules = schedules;
    }

    public Set<ClientCourse> getClientCourses() {
        return clientCourses;
    }

    public void setClientCourses(Set<ClientCourse> clientCourses) {
        this.clientCourses = clientCourses;
    }
}
