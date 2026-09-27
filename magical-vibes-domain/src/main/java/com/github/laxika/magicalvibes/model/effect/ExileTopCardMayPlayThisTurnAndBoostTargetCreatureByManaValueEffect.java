package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top card of the controller's library, lets them play it until end of turn, and gives
 * the target creature +X/+0 until end of turn, where X is that card's mana value.
 */
public record ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect()
        implements CreatureBoostEffect {

    @Override
    public DynamicAmount powerBoost() {
        return new EventValue();
    }

    @Override
    public DynamicAmount toughnessBoost() {
        return new Fixed(0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
