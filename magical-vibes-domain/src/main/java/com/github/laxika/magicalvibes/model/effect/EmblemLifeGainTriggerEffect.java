package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Marker payload for an emblem that triggers whenever its controller gains life.
 * The effects are put on the stack by the life-gain trigger collector, with the gained amount
 * available through {@link com.github.laxika.magicalvibes.model.amount.EventValue}.
 */
public interface EmblemLifeGainTriggerEffect extends CardEffect {

    List<CardEffect> effects();

    /** Concrete marker placed in an emblem's static-effects list. */
    record Marker(List<CardEffect> effects) implements EmblemLifeGainTriggerEffect {

        public Marker {
            effects = List.copyOf(effects);
        }
    }
}
