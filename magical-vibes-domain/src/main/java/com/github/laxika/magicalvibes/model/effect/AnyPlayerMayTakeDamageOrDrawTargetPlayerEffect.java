package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.UUID;

/**
 * Players in turn order may have the source spell deal damage to themselves. If every player
 * declines, the target player draws the specified number of cards.
 */
public record AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect(
        int damage,
        int drawCount,
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId
) implements DamageDealingEffect {

    public AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect(int damage, int drawCount) {
        this(damage, drawCount, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
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
}
