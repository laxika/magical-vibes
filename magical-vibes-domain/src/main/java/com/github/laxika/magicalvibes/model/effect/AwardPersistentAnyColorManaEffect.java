package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Adds mana of one chosen color that remains in the mana pool until end of turn. */
public record AwardPersistentAnyColorManaEffect(DynamicAmount amount)
        implements CombatDamageAmountAwareEffect {

    public AwardPersistentAnyColorManaEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return amount;
    }
}
