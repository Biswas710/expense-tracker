package com.expense_tracker.controller;

import com.expense_tracker.model.Expense;
import com.expense_tracker.model.User;
import com.expense_tracker.repository.ExpenseRepository;
import com.expense_tracker.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.security.Principal;
import java.util.*;

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
    @GetMapping("/analytics")
    public String analytics(Model model,
                            Principal principal) {

        String username = principal.getName();

        User user = userRepo.findByUsername(username);

        List<Expense> expenses = repo.findByUser(user);

        // Month-wise totals
        Map<String, Double> monthlyData = new LinkedHashMap<>();

        for (Expense expense : expenses) {

            if (expense.getDate() != null) {

                String month =
                        expense.getDate().getMonth().toString();

                monthlyData.put(
                        month,
                        monthlyData.getOrDefault(month, 0.0)
                                + expense.getAmount()
                );
            }
        }

        model.addAttribute("monthlyData", monthlyData);
        Map<String, Double> categoryData = new LinkedHashMap<>();

        for (Expense expense : expenses) {

            if (expense.getDate() != null &&

                    expense.getDate().getMonthValue() ==
                            LocalDate.now().getMonthValue() &&

                    expense.getDate().getYear() ==
                            LocalDate.now().getYear()) {

                String category = expense.getCategory();

                categoryData.put(

                        category,

                        categoryData.getOrDefault(category, 0.0)

                                + expense.getAmount()
                );

            }
        }
        model.addAttribute("categoryData", categoryData);

        return "analytics";
    }
    @GetMapping("/profile")
    public String profile(Model model,
                          Principal principal) {

        String username = principal.getName();

        User user =
                userRepo.findByUsername(username);

        List<Expense> expenses =
                repo.findByUser(user);

        double totalExpense =
                expenses.stream()
                        .mapToDouble(Expense::getAmount)
                        .sum();

        String topCategory = expenses.stream()

                .collect(java.util.stream.Collectors.groupingBy(

                        Expense::getCategory,

                        java.util.stream.Collectors.summingDouble(
                                Expense::getAmount
                        )
                ))

                .entrySet()

                .stream()

                .max(java.util.Map.Entry.comparingByValue())

                .map(java.util.Map.Entry::getKey)

                .orElse("No Data");

        model.addAttribute("user", user);

        model.addAttribute("totalExpense",
                totalExpense);

        model.addAttribute("totalTransactions",
                expenses.size());

        model.addAttribute("topCategory",
                topCategory);

        return "profile";
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