package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Static effect: this creature can't attack or block unless its controller pays
 * {@code amountPerCounter} generic mana for each counter of {@code counterType} on it.
 * The combat services read the attack and block costs when the corresponding declaration is made.
 */
public record CantAttackOrBlockUnlessPaysPerCounterEffect(CounterType counterType, int amountPerCounter)
        implements AttackCostEffect, BlockCostEffect {

    @Override
    public int attackCost(Permanent creature) {
        return creature.getCounterCount(counterType) * amountPerCounter;
    }

    @Override
    public int blockCost(Permanent blocker, int attackerPower) {
        return blocker.getCounterCount(counterType) * amountPerCounter;
    }
}
