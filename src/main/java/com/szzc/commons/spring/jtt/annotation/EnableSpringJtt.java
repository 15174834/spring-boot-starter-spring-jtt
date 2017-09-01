package com.szzc.commons.spring.jtt.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.szzc.commons.spring.jtt.config.SpringJttAutoConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SpringJttAutoConfiguration.class)
@Documented
public @interface EnableSpringJtt {
}