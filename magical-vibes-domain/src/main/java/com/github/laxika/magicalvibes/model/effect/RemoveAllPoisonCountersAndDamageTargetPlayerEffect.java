package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerPoisonCounters;

/**
 * "Target player loses all poison counters. This deals that much damage to that player." (Leeches)
 *
 * <p>One effect rather than a removal plus a damage effect because the damage amount is the number
 * of counters actually removed, read before the removal and applied atomically with it.
 */
public record RemoveAllPoisonCountersAndDamageTargetPlayerEffect() implements DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new TargetPlayerPoisonCounters();
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
