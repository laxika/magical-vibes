package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Deals damage to a legal opponent chosen uniformly at random as the triggered ability is put on
 * the stack.
 */
public record DealDamageToRandomOpponentEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToRandomOpponentEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public boolean targetChosenAtRandom() {
        return true;
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
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
