package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Removes up to {@code amount} counters of {@code counterType} from the permanent named by
 * {@code reference}. The reference is resolved when the effect resolves, so an Aura can remove a
 * counter from its current enchanted permanent without targeting that permanent.
 */
public record RemoveCounterFromReferencedPermanentEffect(
        PermanentReference reference,
        CounterType counterType,
        int amount
) implements CardEffect {

    public RemoveCounterFromReferencedPermanentEffect {
        if (reference == PermanentReference.SOURCE) {
            throw new IllegalArgumentException(
                    "PermanentReference.SOURCE is not supported here — use RemoveCounterFromSourceEffect");
        }
    }

    public RemoveCounterFromReferencedPermanentEffect(PermanentReference reference, CounterType counterType) {
        this(reference, counterType, 1);
    }

    public RemoveCounterFromReferencedPermanentEffect(CounterType counterType) {
        this(PermanentReference.ATTACHED, counterType, 1);
    }
}
