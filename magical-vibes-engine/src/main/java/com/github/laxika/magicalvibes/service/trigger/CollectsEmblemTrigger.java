package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a method as a handler for a specific effect stored on an emblem. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(CollectsEmblemTriggers.class)
public @interface CollectsEmblemTrigger {

    Class<? extends CardEffect> value();
}
