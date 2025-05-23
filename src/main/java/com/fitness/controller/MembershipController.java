package com.fitness.controller;

import com.fitness.model.Member;
import com.fitness.model.MembershipPlan;
import com.fitness.service.MembershipService;
import com.fitness.service.MembershipPlanService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(urlPatterns = {"/membership/register"})
public class MembershipController extends HttpServlet {
    
    private MembershipService membershipService;
    private MembershipPlanService membershipPlanService;
    
    @Override
    public void init() throws ServletException {
        String memberFilePath = "src/main/webapp/WEB-INF/data/members.csv";
        membershipService = new MembershipService(memberFilePath);
        membershipPlanService = new MembershipPlanService();
    }
    
    private List<MembershipPlan> getHardcodedPlans() {
        MembershipPlan basic = new MembershipPlan("Basic", "Access to gym equipment, Basic fitness assessment, Group classes (limited)", new BigDecimal("29.99"), 1, "BASIC");
        basic.setId(1L);
        MembershipPlan premium = new MembershipPlan("Premium", "All Basic features, Personal trainer sessions, Unlimited group classes, Nutrition consultation", new BigDecimal("49.99"), 3, "PREMIUM");
        premium.setId(2L);
        MembershipPlan elite = new MembershipPlan("Elite", "All Premium features, VIP lounge access, Spa & sauna access, Priority booking", new BigDecimal("79.99"), 12, "ELITE");
        elite.setId(3L);
        return List.of(basic, premium, elite);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String planId = request.getParameter("planId");
        if (planId == null || planId.trim().isEmpty()) {
            request.setAttribute("error", "Invalid or missing plan selected.");
            request.getRequestDispatcher("/WEB-INF/views/membership/register.jsp").forward(request, response);
            return;
        }
        
        try {
            Long planIdLong = Long.parseLong(planId);
            MembershipPlan selectedPlan = getHardcodedPlans().stream().filter(p -> p.getId().equals(planIdLong)).findFirst().orElse(null);
            if (selectedPlan == null) {
                request.setAttribute("error", "Invalid or missing plan selected.");
            } else {
                request.setAttribute("plan", selectedPlan);
            }
            request.getRequestDispatcher("/WEB-INF/views/membership/register.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid plan ID format.");
            request.getRequestDispatcher("/WEB-INF/views/membership/register.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String planId = request.getParameter("planId");
        
        if (planId == null || planId.trim().isEmpty()) {
            request.setAttribute("error", "Invalid plan selected.");
            response.sendRedirect(request.getContextPath() + "/membership/register?planId=" + planId);
            return;
        }
        
        try {
            Long planIdLong = Long.parseLong(planId);
            MembershipPlan plan = getHardcodedPlans().stream().filter(p -> p.getId().equals(planIdLong)).findFirst().orElse(null);
            if (plan == null) {
                request.setAttribute("error", "Invalid plan selected.");
                response.sendRedirect(request.getContextPath() + "/membership/register?planId=" + planId);
                return;
            }
            
            // Validate phone number (E.164 format)
            if (!phone.matches("^\\+?[1-9]\\d{1,14}$")) {
                request.setAttribute("error", "Phone number must be in international format, e.g. +1234567890");
                response.sendRedirect(request.getContextPath() + "/membership/register?planId=" + planId);
                return;
            }
            
            // Split name into first and last name
            String[] names = name.trim().split(" ", 2);
            String firstName = names[0];
            String lastName = names.length > 1 ? names[1] : "";
            
            // Use a valid placeholder address
            String address = "123 Main St";
            
            membershipService.subscribe(
                firstName,
                lastName,
                email,
                phone,
                LocalDate.of(2000, 1, 1), // Placeholder DOB
                address,
                plan
            );
            
            response.sendRedirect(request.getContextPath() + "/membership/status?email=" + email);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid plan ID format.");
            response.sendRedirect(request.getContextPath() + "/membership/register?planId=" + planId);
        } catch (Exception e) {
            request.setAttribute("error", "An error occurred during registration: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/membership/register?planId=" + planId);
        }
    }
} 