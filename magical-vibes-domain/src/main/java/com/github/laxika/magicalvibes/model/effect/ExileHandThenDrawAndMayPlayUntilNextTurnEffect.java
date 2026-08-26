package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Exiles the controller's hand, draws that many cards, and grants play permission for the exiled cards. */
public record ExileHandThenDrawAndMayPlayUntilNextTurnEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new CardsInHand(CountScope.CONTROLLER);
    }
}
