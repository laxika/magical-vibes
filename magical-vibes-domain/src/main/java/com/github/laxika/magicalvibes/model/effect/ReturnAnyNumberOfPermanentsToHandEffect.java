package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses any number of matching permanents to return to their owners' hands
 * during resolution. The number actually returned is recorded on the resolving stack entry for
 * a following {@code EventValue} effect.
 */
public record ReturnAnyNumberOfPermanentsToHandEffect(PermanentPredicate filter) implements CardEffect {
}
