package com.hc.workmate.controller;

import org.springframework.web.bind.annotation.RequestMapping;

public class HomeController {
	@RequestMapping("/")
    public String viewHome() {
        return "index";
    }
}
