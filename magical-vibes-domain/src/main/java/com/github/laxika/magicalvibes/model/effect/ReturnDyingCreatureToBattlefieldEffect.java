package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;


/**
 * Triggered effect: return a creature card that just died from a graveyard to the battlefield
 * under the ability controller's control, optionally attaching the source Equipment to it.
 *
 * <p>Nim Deathmantle's "you may pay {4}" trigger uses {@code attachSource = true}; Oathkeeper,
 * Takeno's Daisho's equipped-creature-death trigger uses {@code false} (it simply reanimates the
 * Samurai and stays unattached).
 *
 * <p>The dying creature's card is the stack entry's triggering card; a card that left the graveyard and came back
 * after the ability triggered is a new object (CR 400.7) and is not returned.
 *
 * @param attachSource whether the source Equipment attaches to the returned creature
 * @param enterWithCounter the counter put on the returned creature, if any
 * @param enterWithCounterCount the number of counters put on the returned creature
 */
public record ReturnDyingCreatureToBattlefieldEffect(boolean attachSource, CounterType enterWithCounter,
                                                     int enterWithCounterCount) implements CardEffect {

    public ReturnDyingCreatureToBattlefieldEffect(boolean attachSource) {
        this(attachSource, null, 0);
    }
}
