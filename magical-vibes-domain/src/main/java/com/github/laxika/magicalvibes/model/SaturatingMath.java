package com.github.laxika.magicalvibes.model;

/**
 * Integer arithmetic that clamps to {@code [Integer.MIN_VALUE, Integer.MAX_VALUE]} instead of
 * wrapping. Game numbers are integers with no maximum (CR 107.1), but power, toughness, damage
 * and life are stored as {@code int}; effects that double or multiply (Exponential Growth, damage
 * doublers) can exceed that range. Saturating keeps a huge value huge instead of flipping its sign.
 * Once a value has saturated, later arithmetic on it is no longer exact — an accepted deviation,
 * since anything that large is already lethal to every player and creature.
 */
public final class SaturatingMath {

    private SaturatingMath() {
    }

    public static int clamp(long value) {
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, value));
    }

    public static int add(int left, int right) {
        return clamp((long) left + right);
    }

    public static int subtract(int left, int right) {
        return clamp((long) left - right);
    }

    public static int multiply(int left, int right) {
        return clamp((long) left * right);
    }
}
