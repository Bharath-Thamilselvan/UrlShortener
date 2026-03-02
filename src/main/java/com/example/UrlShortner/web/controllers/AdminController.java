package com.example.UrlShortner.web.controllers;

import com.example.UrlShortner.ApplicationProperties;
import com.example.UrlShortner.domain.entities.ShortUrl;
import com.example.UrlShortner.domain.models.PagedResult;
import com.example.UrlShortner.domain.models.ShortUrlDto;
import com.example.UrlShortner.domain.services.ShortUrlService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ShortUrlService shortUrlService;
    private  final ApplicationProperties properties;

    public AdminController(ShortUrlService shortUrlService,ApplicationProperties properties){
        this.shortUrlService=shortUrlService;
        this.properties=properties;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "1") int pageNo,
            Model model
    ){
        PagedResult<ShortUrlDto> allUrls=shortUrlService.findAllShortUrls(pageNo,properties.pageSize());
        model.addAttribute("baseUrl",properties.baseUrl());
        model.addAttribute("shortUrls",allUrls);
        model.addAttribute("paginationUrl","/admin/dashboard");
        return "admin-dashboard";
    }

}
