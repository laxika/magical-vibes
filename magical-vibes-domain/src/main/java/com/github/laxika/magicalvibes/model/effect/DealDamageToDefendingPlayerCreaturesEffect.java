package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals an amount of damage to each matching creature controlled by the player being attacked.
 * The attacked player or planeswalker is preserved as non-targeting combat context on the stack
 * entry, so no player target is chosen. A {@code null} filter matches every defending creature.
 */
public record DealDamageToDefendingPlayerCreaturesEffect(DynamicAmount damage, PermanentPredicate filter)
        implements CombatDamageTriggerContextEffect {

    public DealDamageToDefendingPlayerCreaturesEffect(int damage, PermanentPredicate filter) {
        this(new Fixed(damage), filter);
    }

    public DealDamageToDefendingPlayerCreaturesEffect(DynamicAmount damage) {
        this(damage, null);
    }

    public DealDamageToDefendingPlayerCreaturesEffect(int damage) {
        this(damage, null);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
