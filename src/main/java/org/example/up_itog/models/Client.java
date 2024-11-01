package org.example.up_itog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fullName;

    @Min(16)
    @Max(65)
    private Integer age;

    @OneToMany(mappedBy = "student", cascade = CascadeType.REFRESH, orphanRemoval = true)
    private Set<Grade> grades;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "group_id", nullable = false)
    private Groupss group;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserTable userTable;

    @OneToMany(mappedBy = "client", cascade = CascadeType.REFRESH, orphanRemoval = true)
    private Set<ClientCourse> clientCourses;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Set<Grade> getGrades() {
        return grades; // Добавленный геттер
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades; // Добавленный сеттер
    }

    public Groupss getGroup() {
        return group;
    }

    public void setGroup(Groupss group) {
        this.group = group;
    }

    public UserTable getUserTable() {
        return userTable;
    }

    public void setUserTable(UserTable userTable) {
        this.userTable = userTable;
    }

    public Set<ClientCourse> getClientCourses() {
        return clientCourses;
    }

    public void setClientCourses(Set<ClientCourse> clientCourses) {
        this.clientCourses = clientCourses;
    }
}
