package com.example.UrlShortner.web.controllers;

import com.example.UrlShortner.ApplicationProperties;
import com.example.UrlShortner.domain.entities.ShortUrl;
import com.example.UrlShortner.domain.exceptions.ShortUrlNotFoundException;
import com.example.UrlShortner.domain.models.CreateShortUrlCmd;
import com.example.UrlShortner.domain.models.PagedResult;
import com.example.UrlShortner.domain.models.ShortUrlDto;
import com.example.UrlShortner.domain.services.SecurityUtils;
import com.example.UrlShortner.domain.services.ShortUrlService;
import com.example.UrlShortner.web.dtos.CreateShortUrlForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ShortUrlService shortUrlService;
    private final ApplicationProperties properties;
    private final SecurityUtils securityUtils;

    public HomeController(ShortUrlService shortUrlService,ApplicationProperties properties,SecurityUtils securityUtils){
        this.shortUrlService=shortUrlService;
        this.properties=properties;
        this.securityUtils=securityUtils;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "1") Integer pageNo,
            Model model) {
        this.addShortUrlsDataToModel(model,pageNo);
        model.addAttribute("paginationUrl","/");
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm("",false,null));
        return "index";
    }

    private  void addShortUrlsDataToModel(Model model,int pageNo){
        PagedResult<ShortUrlDto> shortUrlList=shortUrlService.findAllPublicShortUrls(pageNo,properties.pageSize());
        model.addAttribute("shortUrls", shortUrlList);
        model.addAttribute("baseUrl",properties.baseUrl());
    }

    @PostMapping("/short-urls")
    public String createShortUrl(@ModelAttribute("createShortUrlForm") @Valid CreateShortUrlForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model){
        if(bindingResult.hasErrors()){
            this.addShortUrlsDataToModel(model,1);
            return "index";
        }
        try{
            Long userId=securityUtils.getCurrentUserId();
            CreateShortUrlCmd cmd=new CreateShortUrlCmd(
                    form.originalUrl(),
                    form.isPrivate(),
                    form.expirationInDays(),
                    userId
            );
            var shortUrlDto=shortUrlService.createShortUrl(cmd);
            redirectAttributes.addFlashAttribute("successMessage","Short URL created successfully "
            +properties.baseUrl()+"/s/"+shortUrlDto.shortKey());
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage","Failed to create short URL");
        }
        return "redirect:/";
    }

    @GetMapping("/s/{shortKey}")
    String redirectToOriginalUrl(@PathVariable String shortKey){
        Long userId=securityUtils.getCurrentUserId();
        Optional<ShortUrlDto> shortUrlDtoOptional=shortUrlService.accessShortUrl(shortKey,userId);
        if(shortUrlDtoOptional.isEmpty()){
            throw new ShortUrlNotFoundException("Invalid short key: "+shortKey);
        }
        ShortUrlDto shortUrlDto=shortUrlDtoOptional.get();
        return "redirect:"+shortUrlDto.originalUrl();
    }

    @GetMapping("/login")
    String loginForm(){
        return "login";
    }


    @GetMapping("/my-urls")
    public String showUserUrls(
            @RequestParam(defaultValue = "1") int pageNo,
            Model model
    ){
        var currentUserId=securityUtils.getCurrentUserId();
        PagedResult<ShortUrlDto> myUrls = shortUrlService.getUserShortUrls(currentUserId,pageNo,properties.pageSize());
        model.addAttribute("shortUrls",myUrls);
        model.addAttribute("baseUrl",properties.baseUrl());
        model.addAttribute("paginationUrl","/my-urls");
        return "my-urls";
    }

    @PostMapping("/delete-urls")
    public String deleteUrls(
            @RequestParam(value = "ids") List<Long> ids,
            RedirectAttributes redirectAttributes
    ){
        if(ids==null || ids.isEmpty()){
            redirectAttributes.addFlashAttribute(
                    "errorMessage","No URLS selected for deletion");
            return "redirect:/my-urls";
        }
        try {
            var currentUserId=securityUtils.getCurrentUserId();
            shortUrlService.deleteUserShortUrls(ids,currentUserId);
            redirectAttributes.addFlashAttribute(
                    "successMessage","Selected URLS have been deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage","Error deleting URLS: "+e.getMessage());
        }
        return "redirect:/my-urls";
    }


}
