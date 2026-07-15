package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Target permanent becomes {@link #color} until end of turn (CR 105.3 / layer 5).
 *
 * <p>{@link #additive} is {@code false} for the usual "becomes [color]" wording, which replaces all
 * previous colors (Distorting Lens, Grand Architect). It is {@code true} for "becomes [color] in
 * addition to its other colors" (Indigo Faerie), which adds the color instead of replacing.
 */
public record GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive) implements CardEffect {

    public GrantColorUntilEndOfTurnEffect(CardColor color) {
        this(color, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
