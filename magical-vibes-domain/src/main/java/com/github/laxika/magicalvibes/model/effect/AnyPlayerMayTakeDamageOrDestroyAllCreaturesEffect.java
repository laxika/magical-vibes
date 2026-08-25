package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.UUID;

/**
 * Players in turn order may have the source spell deal damage to themselves. If every player
 * declines, all creatures are destroyed without allowing regeneration.
 */
public record AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect(
        int damage,
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId
) implements DamageDealingEffect, BoardWipeEffect {

    public AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect(int damage) {
        this(damage, null, null);
    }

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

    @Override
    public boolean damagesController() {
        return true;
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
