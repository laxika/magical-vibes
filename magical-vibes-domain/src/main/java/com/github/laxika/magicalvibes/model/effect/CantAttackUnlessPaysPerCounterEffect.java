package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Static effect: this creature can't attack unless its controller pays {@code amountPerCounter}
 * generic mana for each counter of {@code counterType} on it (Phyrexian Marauder —
 * {@code PLUS_ONE_PLUS_ONE}, {1}). Read at declare-attackers time via {@link AttackCostEffect}.
 */
public record CantAttackUnlessPaysPerCounterEffect(CounterType counterType, int amountPerCounter)
        implements AttackCostEffect {

    @Override
    public int attackCost(Permanent creature) {
        return creature.getCounterCount(counterType) * amountPerCounter;
    }
}
