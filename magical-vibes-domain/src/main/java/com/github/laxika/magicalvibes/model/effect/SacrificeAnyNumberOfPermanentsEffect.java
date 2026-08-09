package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses any number of matching permanents to sacrifice during resolution.
 * The number actually sacrificed is recorded on the resolving stack entry for a following
 * {@link com.github.laxika.magicalvibes.model.amount.EventValue} effect.
 *
 * @param filter which permanents the controller may sacrifice
 */
public record SacrificeAnyNumberOfPermanentsEffect(PermanentPredicate filter) implements CardEffect {
}
