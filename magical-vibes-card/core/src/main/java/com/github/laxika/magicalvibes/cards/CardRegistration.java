package com.github.laxika.magicalvibes.cards;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CardRegistrations.class)
public @interface CardRegistration {
    String set();
    String collectorNumber();
}
