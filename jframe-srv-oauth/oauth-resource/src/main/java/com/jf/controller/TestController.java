package com.jf.controller;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: admin
 * Date: 2018-06-06
 * Time: 14:59
 */
@RestController
@EnableResourceServer
public class TestController extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests().antMatchers("/monitor/**").authenticated()
                .anyRequest().permitAll()
        ;
    }

    @RequestMapping("/")
    public String index() {
        return "oauth-resource-index";
    }

    @RequestMapping("/monitor/a")
    public String monitor() {
        return "oauth-resource-monitor";
    }

}
