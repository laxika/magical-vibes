package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses any number of matching permanents and removes up to one counter of the
 * specified type from each chosen permanent. The number actually removed is recorded as the
 * resolving stack entry's event value for a following {@link com.github.laxika.magicalvibes.model.amount.EventValue}
 * effect.
 */
public record RemoveCounterFromChosenPermanentsEffect(CounterType counterType,
                                                       PermanentPredicate permanentFilter)
        implements CardEffect {
}
