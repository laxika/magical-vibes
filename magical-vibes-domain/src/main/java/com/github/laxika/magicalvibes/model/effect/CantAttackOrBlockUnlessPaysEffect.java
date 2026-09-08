package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Static effect: this creature can't attack or block unless its controller pays {@code amount}
 * generic mana as an additional combat cost.
 */
public record CantAttackOrBlockUnlessPaysEffect(DynamicAmount amount)
        implements AttackCostEffect, BlockCostEffect {

    public CantAttackOrBlockUnlessPaysEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public int attackCost(Permanent creature) {
        return fixedAmount();
    }

    @Override
    public int blockCost(Permanent blocker, int attackerPower) {
        return fixedAmount();
    }

    @Override
    public DynamicAmount dynamicAttackCost() {
        return amount;
    }

    @Override
    public DynamicAmount dynamicBlockCost() {
        return amount;
    }

    private int fixedAmount() {
        return amount instanceof Fixed fixed ? fixed.value() : 0;
    }
}
