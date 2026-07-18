package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller draws a card and reveals it; if the revealed card isn't a land card, it is
 * discarded. Net card-filtering: only land cards are kept. Used by Sindbad ({@code {T}:}).
 */
public record DrawCardRevealDiscardUnlessLandEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
