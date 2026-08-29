package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the concrete card classes whose oracle data a card test needs.
 *
 * <p>The annotation may be placed on a test class for cards shared by all its test methods, or on
 * an individual method for cards used only by that scenario. {@link BaseCardTest} preloads their
 * sets before test setup. List every concrete card constructed by the annotated scope, including
 * support cards. The preloader groups those classes by their registered printings and favors the
 * set covering the most cards, minimizing the number of oracle sets loaded for the test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CardUsed {

    Class<? extends Card>[] value();
}
