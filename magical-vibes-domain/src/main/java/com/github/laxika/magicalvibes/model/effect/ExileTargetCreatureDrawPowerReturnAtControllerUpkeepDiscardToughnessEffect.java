package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetPower;

/**
 * Exiles a target creature, draws based on its power before it leaves, and schedules its return
 * under its owner's control at the effect controller's next upkeep. The return follow-up discards
 * based on the returned permanent's toughness.
 */
public record ExileTargetCreatureDrawPowerReturnAtControllerUpkeepDiscardToughnessEffect()
        implements RemovalEffect, CardDrawingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new TargetPower();
    }
}
