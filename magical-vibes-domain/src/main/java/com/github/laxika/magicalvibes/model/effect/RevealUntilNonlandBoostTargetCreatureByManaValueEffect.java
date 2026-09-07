package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;

/**
 * Reveals cards from the controller's library until a nonland card is revealed, then gives the
 * target creature +X/-X until end of turn, where X is that card's mana value. All revealed cards
 * are put on the bottom of the library in any order.
 */
public record RevealUntilNonlandBoostTargetCreatureByManaValueEffect() implements CreatureBoostEffect {

    @Override
    public DynamicAmount powerBoost() {
        return new EventValue();
    }

    @Override
    public DynamicAmount toughnessBoost() {
        return new Scaled(new EventValue(), -1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
