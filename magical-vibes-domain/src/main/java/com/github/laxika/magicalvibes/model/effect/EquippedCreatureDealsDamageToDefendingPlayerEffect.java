package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The creature equipped by the source Equipment deals damage to the player being attacked.
 * The combat trigger records that creature in {@code StackEntry.triggeringPermanentId} and the
 * attacked player or planeswalker in {@code StackEntry.attackedTargetId}.
 */
public record EquippedCreatureDealsDamageToDefendingPlayerEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public EquippedCreatureDealsDamageToDefendingPlayerEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
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
