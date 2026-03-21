package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts counter(s) of the specified type on a permanent.
 *
 * <p>When {@code predicate} is {@code null}, this is a targeting effect — the permanent is
 * chosen as a target when the spell or ability is cast. For lore counters on Sagas, this also
 * triggers the appropriate chapter ability per MTG Rule 714.3b.</p>
 *
 * <p>When {@code predicate} is non-null, this is a resolution-time choice — the controller
 * chooses a permanent they control matching the predicate during resolution. If no permanents
 * match, the effect does nothing. If exactly one matches, it is automatically chosen.
 * If multiple match, the controller chooses one.</p>
 */
public record PutCounterOnTargetPermanentEffect(CounterType counterType, int count,
                                                 PermanentPredicate predicate) implements CardEffect {

    public PutCounterOnTargetPermanentEffect(CounterType counterType) {
        this(counterType, 1, null);
    }

    @Override
    public boolean canTargetPermanent() { return predicate == null; }
}
