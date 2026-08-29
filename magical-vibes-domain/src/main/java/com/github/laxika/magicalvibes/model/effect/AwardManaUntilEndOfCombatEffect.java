package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Adds mana that remains in the mana pool until the current combat ends. */
public record AwardManaUntilEndOfCombatEffect(ManaColor color, DynamicAmount amount)
        implements CardEffect {

    public AwardManaUntilEndOfCombatEffect(ManaColor color, int amount) {
        this(color, new Fixed(amount));
    }
}
