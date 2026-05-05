package com.expense_tracker.controller;

import com.expense_tracker.model.Expense;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ExpenseController {

    @GetMapping("/add-expense")
    public String showForm(Model model) {
        model.addAttribute("expense", new Expense());
        return "add-expense";
    }

    @PostMapping("/save-expense")
    public String saveExpense(@ModelAttribute Expense expense, Model model) {
        model.addAttribute("message", "Expense Added: " + expense.getTitle());
        return "result";
    }
}