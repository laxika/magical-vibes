package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses any number of matching permanents to sacrifice during resolution.
 * The number actually sacrificed is recorded on the resolving stack entry for a following
 * {@link com.github.laxika.magicalvibes.model.amount.EventValue} effect.
 *
 * @param filter which permanents the controller may sacrifice
 * @param recordSacrificedPower whether to snapshot the selected permanents' total effective power
 */
public record SacrificeAnyNumberOfPermanentsEffect(PermanentPredicate filter,
                                                    boolean recordSacrificedPower)
        implements CardEffect {

    public SacrificeAnyNumberOfPermanentsEffect(PermanentPredicate filter) {
        this(filter, false);
    }
}
