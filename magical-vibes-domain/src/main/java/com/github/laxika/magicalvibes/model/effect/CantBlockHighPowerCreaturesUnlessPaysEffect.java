package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature can't block creatures with power {@code minAttackerPower} or greater
 * unless its controller pays {@code amount} generic mana as an additional cost to declare the block
 * (Hipparion — power 3 or greater, {1}). Read at declare-blockers time via {@link BlockCostEffect}.
 */
public record CantBlockHighPowerCreaturesUnlessPaysEffect(int minAttackerPower, int amount)
        implements BlockCostEffect {

    @Override
    public int blockCost(int attackerPower) {
        return attackerPower >= minAttackerPower ? amount : 0;
    }
}
