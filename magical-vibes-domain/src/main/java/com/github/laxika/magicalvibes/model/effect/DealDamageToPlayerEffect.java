package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.UUID;

/**
 * Deals fixed damage to a remembered player without targeting that player.
 *
 * <p>The player ID is stored when the effect is created, so it can represent effects such as
 * Takklemaggot's fallback ability that refer to the player who made an earlier choice.</p>
 */
public record DealDamageToPlayerEffect(int damage, UUID playerId)
        implements DamageDealingEffect, PlayerSpecificUpkeepEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
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
