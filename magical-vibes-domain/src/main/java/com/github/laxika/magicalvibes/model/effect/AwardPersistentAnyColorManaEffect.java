package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

/** Adds mana of one chosen color that remains in the mana pool until end of turn. */
public record AwardPersistentAnyColorManaEffect(DynamicAmount amount,
                                                ManaSpendRestriction restriction,
                                                boolean anyColorCombination) implements CardEffect {

    public AwardPersistentAnyColorManaEffect(DynamicAmount amount) {
        this(amount, ManaSpendRestriction.NONE, false);
    }

    public AwardPersistentAnyColorManaEffect(int amount) {
        this(new Fixed(amount));
    }
}
