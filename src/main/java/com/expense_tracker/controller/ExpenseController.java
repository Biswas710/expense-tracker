package com.expense_tracker.controller;

import com.expense_tracker.model.Expense;
import com.expense_tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ExpenseController {

    private final ExpenseRepository repo;

    public ExpenseController(ExpenseRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/add-expense")
    public String showForm(Model model) {
        model.addAttribute("expense", new Expense());
        return "add-expense";
    }

    @PostMapping("/save-expense")
    public String saveExpense(@ModelAttribute Expense expense) {
        repo.save(expense);
        return "redirect:/expenses";
    }

    @GetMapping("/expenses")
    public String showExpenses(Model model) {
        model.addAttribute("expenses", repo.findAll());
        return "expenses";
    }
}