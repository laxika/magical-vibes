package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Static effect: this creature can't attack or block unless its controller pays {@code amount}
 * generic mana as an additional combat cost.
 */
public record CantAttackOrBlockUnlessPaysEffect(int amount)
        implements AttackCostEffect, BlockCostEffect {

    @Override
    public int attackCost(Permanent creature) {
        return amount;
    }

    @Override
    public int blockCost(Permanent blocker, int attackerPower) {
        return amount;
    }
}
