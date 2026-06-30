package com.expense_tracker.controller;

import com.expense_tracker.model.Budget;
import com.expense_tracker.model.Expense;
import com.expense_tracker.model.User;
import com.expense_tracker.repository.ExpenseRepository;
import com.expense_tracker.repository.UserRepository;
import com.expense_tracker.repository.BudgetRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.security.Principal;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
public class ExpenseController {

    private final ExpenseRepository repo;

    private final UserRepository userRepo;

    private final BudgetRepository budgetRepo;

    // Constructor Injection
    public ExpenseController(ExpenseRepository repo,
                             UserRepository userRepo, BudgetRepository budgetRepo) {

        this.repo = repo;
        this.userRepo = userRepo;
        this.budgetRepo = budgetRepo;
    }
    @GetMapping("/analytics")
    public String analytics(Model model,
                            Principal principal,
                            @RequestParam(value = "month", required = false) Integer month,
                            @RequestParam(value = "year", required = false) Integer year) {

        String username = principal.getName();

        User user = userRepo.findByUsername(username);

        List<Expense> expenses = repo.findByUser(user);

        LocalDate today = LocalDate.now();

        if(month == null) {
            month = today.getMonthValue();
        }

        if(year == null) {
            year = today.getYear();
        }

        // ==========================
        // BAR CHART (Whole Year)
        // ==========================

        Map<String, Double> monthlyData =
                new LinkedHashMap<>();

        for(Expense expense : expenses) {

            if(expense.getDate() != null &&
                    expense.getDate().getYear() == year) {

                String monthName =
                        expense.getDate()
                                .getMonth()
                                .toString();

                monthlyData.put(

                        monthName,

                        monthlyData.getOrDefault(
                                monthName,
                                0.0
                        ) + expense.getAmount()
                );
            }
        }

        model.addAttribute(
                "monthlyData",
                monthlyData
        );

        // ==========================
        // PIE CHART (Selected Month)
        // ==========================

        Map<String, Double> categoryData =
                new LinkedHashMap<>();

        for(Expense expense : expenses) {

            if(expense.getDate() != null &&

                    expense.getDate()
                            .getMonthValue() == month &&

                    expense.getDate()
                            .getYear() == year) {

                String category =
                        expense.getCategory();

                categoryData.put(

                        category,

                        categoryData.getOrDefault(
                                category,
                                0.0
                        ) + expense.getAmount()
                );
            }
        }

        model.addAttribute(
                "categoryData",
                categoryData
        );

        model.addAttribute(
                "selectedMonth",
                month
        );

        model.addAttribute(
                "selectedYear",
                year
        );

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
    @GetMapping("/budget")
    public String budgetPage() {

        return "budget";
    }
    @PostMapping("/save-budget")
    public String saveBudget(@ModelAttribute Budget budget,
                             Principal principal) {

        String username = principal.getName();

        User user = userRepo.findByUsername(username);

        LocalDate today = LocalDate.now();

        Budget existingBudget =
                budgetRepo.findByUserAndMonthAndYear(
                        user,
                        today.getMonthValue(),
                        today.getYear()
                );

        if(existingBudget != null){

            existingBudget.setMonthlyBudget(
                    budget.getMonthlyBudget()
            );

            budgetRepo.save(existingBudget);

        }else{

            budget.setUser(user);

            budget.setMonth(today.getMonthValue());

            budget.setYear(today.getYear());

            budgetRepo.save(budget);

        }

        return "redirect:/expenses";
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
    public String showExpenses(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model,
                               Principal principal) {

        String username = principal.getName();

        User user = userRepo.findByUsername(username);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("date").descending()
        );

        Page<Expense> expensePage = repo.findByUser(user, pageable);

        List<Expense> expenses = expensePage.getContent();

        // Used only for dashboard calculations
        List<Expense> allExpenses = repo.findByUser(user);

        LocalDate today = LocalDate.now();

        double totalExpense = allExpenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        double monthlyExpense = allExpenses.stream()
                .filter(expense ->
                        expense.getDate() != null &&
                                expense.getDate().getMonthValue() == today.getMonthValue() &&
                                expense.getDate().getYear() == today.getYear())
                .mapToDouble(Expense::getAmount)
                .sum();

        long monthlyTransactions = allExpenses.stream()
                .filter(expense ->
                        expense.getDate() != null &&
                                expense.getDate().getMonthValue() == today.getMonthValue() &&
                                expense.getDate().getYear() == today.getYear())
                .count();

        Budget budget = budgetRepo.findByUserAndMonthAndYear(
                user,
                today.getMonthValue(),
                today.getYear()
        );

        double monthlyBudget = 0;
        double remainingBudget = 0;
        double budgetUsed = 0;
        String warning = "No budget set for this month.";

        if (budget != null) {

            monthlyBudget = budget.getMonthlyBudget();
            remainingBudget = monthlyBudget - monthlyExpense;

            if (monthlyBudget > 0) {
                budgetUsed = (monthlyExpense / monthlyBudget) * 100;
            }

            if (budgetUsed >= 100) {
                warning = "🚨 Budget Exceeded!";
            } else if (budgetUsed >= 80) {
                warning = "⚠️ You have used over 80% of your budget.";
            } else {
                warning = "✅ Budget is under control.";
            }
        }

        model.addAttribute("expenses", expenses);

        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("monthlyExpense", monthlyExpense);
        model.addAttribute("monthlyTransactions", monthlyTransactions);

        model.addAttribute("monthlyBudget", monthlyBudget);
        model.addAttribute("remainingBudget", remainingBudget);
        model.addAttribute("budgetUsed", budgetUsed);
        model.addAttribute("warning", warning);

        // Pagination
        model.addAttribute("currentPage", expensePage.getNumber());
        model.addAttribute("totalPages", expensePage.getTotalPages());

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