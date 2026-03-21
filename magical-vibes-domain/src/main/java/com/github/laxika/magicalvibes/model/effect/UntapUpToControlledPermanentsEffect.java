package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untaps up to {@code count} tapped permanents the controller controls that match {@code filter}.
 * If the controller has fewer matching tapped permanents than {@code count}, all are untapped.
 * If there are more, the first {@code count} (by battlefield order) are untapped.
 * If {@code filter} is null, any tapped permanent qualifies.
 *
 * @param count  the maximum number of tapped permanents to untap
 * @param filter optional predicate to restrict which permanents can be untapped
 */
public record UntapUpToControlledPermanentsEffect(int count, PermanentPredicate filter) implements CardEffect {
}
