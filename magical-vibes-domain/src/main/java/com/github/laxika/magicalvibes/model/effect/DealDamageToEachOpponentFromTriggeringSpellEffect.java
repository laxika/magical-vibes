package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals damage to each opponent using the triggering spell as the damage source. */
public record DealDamageToEachOpponentFromTriggeringSpellEffect(DynamicAmount amount)
        implements TriggeringSpellReferencingEffect, DamageDealingEffect {

    public DealDamageToEachOpponentFromTriggeringSpellEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
