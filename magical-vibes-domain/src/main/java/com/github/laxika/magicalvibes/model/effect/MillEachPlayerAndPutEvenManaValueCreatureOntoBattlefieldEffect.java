package com.github.laxika.magicalvibes.model.effect;

/** Mills each player, then puts one chosen even-mana-value creature card among those cards onto the battlefield. */
public record MillEachPlayerAndPutEvenManaValueCreatureOntoBattlefieldEffect(int count)
        implements CardEffect {

    public MillEachPlayerAndPutEvenManaValueCreatureOntoBattlefieldEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }
}
