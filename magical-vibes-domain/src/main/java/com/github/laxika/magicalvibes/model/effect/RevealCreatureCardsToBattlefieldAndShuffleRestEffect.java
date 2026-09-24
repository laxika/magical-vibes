package com.github.laxika.magicalvibes.model.effect;

/** Reveals until the specified number of creature cards is found, putting those cards onto the battlefield. */
public record RevealCreatureCardsToBattlefieldAndShuffleRestEffect(int creatureCount)
        implements CardEffect {
}
