package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Static defender-scoped attack tax: a creature with counters on it costs generic mana equal to
 * its total counter count to attack this effect's controller.
 */
public record CreaturesWithCountersCantAttackControllerUnlessPaysEffect()
        implements DefenderAttackCostEffect {

    @Override
    public int attackCost(Permanent attacker) {
        return attacker.getTotalCounterCount();
    }
}
