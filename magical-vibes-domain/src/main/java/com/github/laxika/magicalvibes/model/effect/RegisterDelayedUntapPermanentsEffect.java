package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When resolved, registers a delayed trigger that fires at the beginning of the next end step,
 * untapping up to {@code count} permanents the controller controls that match {@code filter}.
 * If {@code filter} is null, any tapped permanent qualifies.
 *
 * @param count  the maximum number of permanents to untap at the next end step
 * @param filter optional predicate to restrict which permanents can be untapped (e.g. lands only)
 */
public record RegisterDelayedUntapPermanentsEffect(int count, PermanentPredicate filter) implements CardEffect {
}
