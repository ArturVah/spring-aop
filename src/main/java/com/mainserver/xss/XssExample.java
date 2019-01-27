package com.mainserver.xss;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@interface Log {
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@interface Secure {
}

@SpringBootApplication
@EnableAspectJAutoProxy
public class XssExample {

    public static void main(String[] args) {
        SpringApplication.run(XssExample.class, args);
    }

}

@RestController
@RequestMapping("/posts")
class PostController {

    private List<String> posts = new ArrayList<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Log
    @GetMapping
    @CrossOrigin
    public ResponseEntity<?> get(@RequestParam(value = "id", required = false) Integer id) {
        System.out.println(applicationContext.getBean(PostController.class));
        return ResponseEntity.ok(
                id == null ? posts : posts.get(id)
        );
    }

    @Secure
    @PostMapping(consumes = "text/plain")
    public ResponseEntity<?> post(@RequestBody String data) {
        posts.add(data);
        return ResponseEntity.ok().build();
    }
}

@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();
    }

}

@Aspect
@Component
class LogAspect {

    @Before("@annotation(com.mainserver.xss.Log)")
    public void before(JoinPoint joinPoint) {
        System.out.println(Arrays.toString(joinPoint.getArgs()));
    }

    @Around("@annotation(com.mainserver.xss.Secure)")
    public Object checkSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        if (checkSecurityHeaders(joinPoint.getArgs())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return joinPoint.proceed();

    }

    private boolean checkSecurityHeaders(Object[] args) {
        return true;
    }

}