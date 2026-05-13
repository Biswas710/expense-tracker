package com.expense_tracker.controller;

import com.expense_tracker.model.Expense;
import com.expense_tracker.model.User;
import com.expense_tracker.repository.ExpenseRepository;
import com.expense_tracker.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class ExpenseController {

    private final ExpenseRepository repo;

    private final UserRepository userRepo;


    // Constructor Injection
    public ExpenseController(ExpenseRepository repo,
                             UserRepository userRepo) {

        this.repo = repo;
        this.userRepo = userRepo;
    }

    @GetMapping("/add-expense")
    public String showForm(Model model) {

        model.addAttribute("expense", new Expense());

        return "add-expense";
    }

    @PostMapping("/save-expense")
    public String saveExpense(@ModelAttribute Expense expense,
                              Principal principal) {

        // Get logged-in username
        String username = principal.getName();

        // Find user from database
        User user = userRepo.findByUsername(username);

        // Attach user to expense
        expense.setUser(user);

        // Save expense
        repo.save(expense);

        return "redirect:/expenses";
    }



    @GetMapping("/expenses")
    public String showExpenses(Model model, Principal principal) {

        String username = principal.getName();

        User user = userRepo.findByUsername(username);

        // Get expenses of logged-in user
        List<Expense> expenses = repo.findByUser(user);

        // Calculate total expense
        double totalExpense = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        // Calculate current month expense
        double monthlyExpense = expenses.stream()
                .filter(expense -> expense.getDate() != null &&
                        expense.getDate().getMonthValue() ==
                                java.time.LocalDate.now().getMonthValue())
                .mapToDouble(Expense::getAmount)
                .sum();

        model.addAttribute("expenses", expenses);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("monthlyExpense", monthlyExpense);

        return "expenses";
    }
    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {

        repo.deleteById(id);

        return "redirect:/expenses";
    }

    @GetMapping("/edit/{id}")
    public String editExpense(@PathVariable Long id,
                              Model model) {

        Expense expense = repo.findById(id).orElse(null);

        model.addAttribute("expense", expense);

        return "add-expense";
    }

    @PostMapping("/update-expense")
    public String updateExpense(@ModelAttribute Expense expense,
                                Principal principal) {

        String username = principal.getName();

        User user = userRepo.findByUsername(username);

        expense.setUser(user);

        repo.save(expense);

        return "redirect:/expenses";
    }
}