package com.github.laxika.magicalvibes.cards;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a {@code Card} subclass as the printing with this collector number in this set; repeat
 * it for each reprint. The class supplies only engine logic: its name, types, cost and text are
 * loaded from the oracle data of the printing named here. When a set is loaded, the oracle name at
 * that collector number must read as the class name, or the set fails to load, so a mistyped
 * collector number cannot give the class another card's characteristics.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CardRegistrations.class)
public @interface CardRegistration {
    String set();
    String collectorNumber();
}
