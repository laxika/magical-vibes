package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "ECL", collectorNumber = "203")
public class WildvinePummeler extends Card {

    public WildvinePummeler() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new ColorsAmongControlledPermanents()));
    }
}
