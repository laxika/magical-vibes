package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Reveals the top card of the controller's library. If it's a land card, put it onto the
 * battlefield tapped; otherwise, draw a card.
 */
public record RevealTopCardLandToTappedBattlefieldElseDrawEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
