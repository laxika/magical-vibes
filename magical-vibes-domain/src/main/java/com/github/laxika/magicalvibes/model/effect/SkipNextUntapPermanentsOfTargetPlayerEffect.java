package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Effect: "Permanents matching the filter that target player controls don't untap
 * during that player's next untap step."
 *
 * <p>Targets a player, then iterates their battlefield and increments
 * {@code skipUntapCount} on every permanent that satisfies the given
 * {@link PermanentPredicate}.</p>
 */
public record SkipNextUntapPermanentsOfTargetPlayerEffect(PermanentPredicate filter) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
