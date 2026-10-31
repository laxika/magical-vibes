package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "ECL", collectorNumber = "203")
public class WildvinePummeler extends Card {

    public WildvinePummeler() {
        addStaticEffect(new ReduceOwnCastCostEffect(new ColorsAmongControlledPermanents()));
    }
}
