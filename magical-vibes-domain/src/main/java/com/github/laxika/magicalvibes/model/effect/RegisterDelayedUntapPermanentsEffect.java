package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When resolved, registers a delayed trigger that fires at the beginning of the next end step,
 * letting its controller untap up to {@code count} tapped permanents on any battlefield that match
 * {@code filter} ("untap up to two lands" does not restrict to the controller's lands).
 * If {@code filter} is null, any tapped permanent qualifies.
 *
 * @param count  the maximum number of permanents to untap at the next end step
 * @param filter optional predicate to restrict which permanents can be untapped (e.g. lands only)
 */
public record RegisterDelayedUntapPermanentsEffect(int count, PermanentPredicate filter) implements CardEffect {
}
