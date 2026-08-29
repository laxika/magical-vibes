package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "PLS", collectorNumber = "135")
public class Stratadon extends Card {

    public Stratadon() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new BasicLandTypesAmongControlledLands()));
    }
}
