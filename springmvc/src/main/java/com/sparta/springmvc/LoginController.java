package com.sparta.springmvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginForm(){
        return "redirect:/login-form.html";
    }

    @PostMapping("/login")
    public String login(@RequestParam String id, @RequestParam String password, Model model){
        if(id.equals(password)){
            model.addAttribute("loginId", id);
        }

        return "login-result";
    }
}
