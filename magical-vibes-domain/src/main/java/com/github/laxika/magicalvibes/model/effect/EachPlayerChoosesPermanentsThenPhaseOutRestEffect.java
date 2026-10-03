package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses up to {@code maxCount} permanents they control in source-controller order,
 * then all other permanents except the source phase out.
 */
public record EachPlayerChoosesPermanentsThenPhaseOutRestEffect(int maxCount) implements CardEffect {

    public EachPlayerChoosesPermanentsThenPhaseOutRestEffect {
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount must not be negative");
        }
    }
}
