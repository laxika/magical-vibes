package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The active player discards their entire hand, then draws a fixed number of cards.
 * The effect retains a player target so an upkeep ability can enforce a separate target
 * restriction while applying the discard and draw to the player whose upkeep caused it.
 */
public record ActivePlayerDiscardsHandThenDrawsEffect(int drawAmount) implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(drawAmount);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
