package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;


@CardRegistration(set = "HOU", collectorNumber = "163")
public class HollowOne extends Card {

    public HollowOne() {
        // This spell costs {2} less to cast for each card you've cycled or discarded this turn.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new Scaled(new CardsDiscardedOrCycledThisTurn(), 2)));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
