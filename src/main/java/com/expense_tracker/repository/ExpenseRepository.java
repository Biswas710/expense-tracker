package com.expense_tracker.repository;

import com.expense_tracker.model.Expense;
import com.expense_tracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    List<Expense> findByUser(User user);
    Page<Expense> findByUser(User user, Pageable pageable);
}