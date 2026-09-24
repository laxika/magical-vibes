package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses any number of matching permanents to sacrifice during resolution,
 * optionally subject to a maximum count.
 * The number actually sacrificed is recorded on the resolving stack entry for a following
 * {@link com.github.laxika.magicalvibes.model.amount.EventValue} effect.
 *
 * @param filter which permanents the controller may sacrifice
 * @param recordSacrificedPower whether to snapshot the selected permanents' total effective power
 * @param maximumCount maximum number of permanents that may be sacrificed, or {@code 0} for no maximum
 */
public record SacrificeAnyNumberOfPermanentsEffect(PermanentPredicate filter,
                                                    boolean recordSacrificedPower,
                                                    int maximumCount)
        implements CardEffect {

    public SacrificeAnyNumberOfPermanentsEffect(PermanentPredicate filter) {
        this(filter, false, 0);
    }

    public SacrificeAnyNumberOfPermanentsEffect(PermanentPredicate filter, boolean recordSacrificedPower) {
        this(filter, recordSacrificedPower, 0);
    }

    public SacrificeAnyNumberOfPermanentsEffect(PermanentPredicate filter, int maximumCount) {
        this(filter, false, maximumCount);
    }

    public SacrificeAnyNumberOfPermanentsEffect(PermanentPredicate filter, boolean recordSacrificedPower,
                                                 int maximumCount) {
        this.filter = filter;
        this.recordSacrificedPower = recordSacrificedPower;
        this.maximumCount = maximumCount;
        if (maximumCount < 0) {
            throw new IllegalArgumentException("maximumCount must be non-negative");
        }
    }
}
