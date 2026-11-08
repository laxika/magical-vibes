package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to "it", the stack entry's triggering permanent (referenced, not targeted), then boosts the source
 * if a permanent matching {@code boostTargetPredicate} was dealt damage this way. Marauding Raptor ("Whenever another
 * creature you control enters, this creature deals 2 damage to it. If a Dinosaur is dealt damage this way, this
 * creature gets +2/+0 until end of turn.").
 */
public record DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect(
        DynamicAmount damage,
        PermanentPredicate boostTargetPredicate,
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost) implements DamageDealingEffect {

    public DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect(int damage,
                                                                      PermanentPredicate boostTargetPredicate,
                                                                      int powerBoost,
                                                                      int toughnessBoost) {
        this(new Fixed(damage), boostTargetPredicate, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
