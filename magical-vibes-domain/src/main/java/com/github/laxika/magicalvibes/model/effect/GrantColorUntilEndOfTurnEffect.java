package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Target permanent becomes {@link #color} until end of turn.
 *
 * <p>{@link #additive} is {@code false} for the usual "becomes [color]" wording, which replaces all
 * previous colors (Distorting Lens, Grand Architect). It is {@code true} for "becomes [color] in
 * addition to its other colors" (Indigo Faerie), which adds the color instead of replacing.
 *
 * <p>{@link #canTargetSpell} is {@code true} for "target permanent or spell becomes [color]"
 * (Eight-and-a-Half-Tails), where a target that is not on the battlefield is looked up on the stack.
 */
public record GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive, GrantScope scope,
                                             boolean canTargetSpell) implements CardEffect {

    public GrantColorUntilEndOfTurnEffect(CardColor color) {
        this(color, false, GrantScope.TARGET, false);
    }

    public GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive) {
        this(color, additive, GrantScope.TARGET, false);
    }

    public GrantColorUntilEndOfTurnEffect(CardColor color, GrantScope scope) {
        this(color, false, scope, false);
    }

    public GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive, boolean canTargetSpell) {
        this(color, additive, GrantScope.TARGET, canTargetSpell);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET_PLAYERS_CREATURES
                ? TargetSpec.benign(TargetCategory.PLAYER)
                : TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
