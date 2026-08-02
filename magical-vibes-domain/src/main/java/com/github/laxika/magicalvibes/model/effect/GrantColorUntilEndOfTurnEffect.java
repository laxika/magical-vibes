package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Target permanent becomes {@link #color} until end of turn.
 *
 * <p>{@link #additive} is {@code false} for the usual "becomes [color]" wording, which replaces all
 * previous colors (Distorting Lens, Grand Architect). It is {@code true} for "becomes [color] in
 * addition to its other colors" (Indigo Faerie), which adds the color instead of replacing.
 */
public record GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive, boolean canTargetSpell)
        implements CardEffect {

    public GrantColorUntilEndOfTurnEffect(CardColor color) {
        this(color, false, false);
    }

    public GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive) {
        this(color, additive, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
