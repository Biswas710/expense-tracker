package com.expense_tracker.controller;

import com.expense_tracker.model.Expense;
import com.expense_tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ExpenseController {

    private final ExpenseRepository repo;

    // Constructor Injection
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

        // Fetch all data from DB
        model.addAttribute("expenses", repo.findAll());

        return "expenses";
    }


    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {

        // Delete by ID
        repo.deleteById(id);

        return "redirect:/expenses";
    }


    @GetMapping("/edit/{id}")
    public String editExpense(@PathVariable Long id, Model model) {
        Expense expense = repo.findById(id).orElse(null);


        model.addAttribute("expense", expense);

        // Reuse same form
        return "add-expense";
    }


    @PostMapping("/update-expense")
    public String updateExpense(@ModelAttribute Expense expense) {

        repo.save(expense);

        return "redirect:/expenses";
    }
}