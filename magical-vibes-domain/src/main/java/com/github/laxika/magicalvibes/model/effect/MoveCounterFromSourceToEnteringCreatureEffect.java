package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Trigger-materialising effect for moving a counter from the source permanent onto the creature
 * that caused an enter-the-battlefield trigger. The entering permanent id is filled in by the
 * enter-trigger collector; the effect is intentionally non-targeting.
 */
public record MoveCounterFromSourceToEnteringCreatureEffect(CounterType counterType,
                                                             UUID enteringPermanentId)
        implements CardEffect {

    public MoveCounterFromSourceToEnteringCreatureEffect(CounterType counterType) {
        this(counterType, null);
    }
}
