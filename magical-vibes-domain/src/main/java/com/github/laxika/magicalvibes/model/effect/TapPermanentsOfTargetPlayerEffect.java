package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Effect: "Tap all permanents matching the filter that target player controls."
 *
 * <p>Targets a player, then iterates their battlefield and taps every permanent
 * that satisfies the given {@link PermanentPredicate}.</p>
 */
public record TapPermanentsOfTargetPlayerEffect(PermanentPredicate filter) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
