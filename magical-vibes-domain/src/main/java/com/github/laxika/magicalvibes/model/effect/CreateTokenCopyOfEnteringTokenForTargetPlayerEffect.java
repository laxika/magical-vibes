package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Creates a token copy of the token that caused the trigger under the targeted player's control.
 * The target player must differ from that token's controller; the trigger controller draws a card
 * when the target player is an opponent.
 */
public record CreateTokenCopyOfEnteringTokenForTargetPlayerEffect()
        implements CardDrawingEffect, TargetPlayerOtherThanTriggeringPermanentControllerEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
