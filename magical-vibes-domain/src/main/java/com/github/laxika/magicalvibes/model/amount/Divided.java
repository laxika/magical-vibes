package com.github.laxika.magicalvibes.model.amount;

/**
 * A base amount divided by a constant, rounded down ("half the number of Zombies you
 * control, rounded down" = {@code Divided(count, 2)}). The integer-division sibling of
 * {@link Scaled}.
 */
public record Divided(DynamicAmount amount, int divisor) implements DynamicAmount {
}
