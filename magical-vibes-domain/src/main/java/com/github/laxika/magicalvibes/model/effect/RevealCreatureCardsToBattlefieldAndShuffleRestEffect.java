package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Reveals until the specified number of creature cards is found, putting those cards onto the battlefield. */
public record RevealCreatureCardsToBattlefieldAndShuffleRestEffect(DynamicAmount creatureCount)
        implements CardEffect {

    public RevealCreatureCardsToBattlefieldAndShuffleRestEffect(int creatureCount) {
        this(new Fixed(creatureCount));
    }
}
