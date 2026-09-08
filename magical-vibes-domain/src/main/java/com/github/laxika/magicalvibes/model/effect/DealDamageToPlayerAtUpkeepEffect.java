package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.UUID;

/**
 * Deals one damage to a fixed player during that player's upkeep.
 */
public record DealDamageToPlayerAtUpkeepEffect(UUID playerId)
        implements UpkeepPlayerDependentEffect, DamageDealingEffect {

    @Override
    public boolean triggersFor(UUID activePlayerId) {
        return playerId != null && playerId.equals(activePlayerId);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(1);
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
