package com.expense_tracker.model;
import jakarta.persistence.*;
@Entity
@Table(
        name = "budget",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "month", "year"}
                )
        }
)
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double monthlyBudget;

    private Integer month;

    private Integer year;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Default Constructor
    public Budget() {
    }

    // Parameterized Constructor
    public Budget(Double monthlyBudget, Integer month, Integer year, User user) {
        this.monthlyBudget = monthlyBudget;
        this.month = month;
        this.year = year;
        this.user = user;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(Double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
