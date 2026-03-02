package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When resolved, all creatures on all battlefields matching the given predicate
 * are marked as unable to block for the remainder of the turn.
 * <p>
 * This is a non-targeted mass effect. Use with any {@link PermanentPredicate}
 * to filter which creatures are affected (e.g. power-based, color-based, subtype-based).
 * Pass {@code null} to affect all creatures.
 */
public record CantBlockThisTurnEffect(PermanentPredicate filter) implements CardEffect {
}
