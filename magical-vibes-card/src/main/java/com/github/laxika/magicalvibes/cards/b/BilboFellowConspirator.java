package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddTreasureToFoodTokenCreationEffect;

@CardRegistration(set = "HOC", collectorNumber = "4")
public class BilboFellowConspirator extends Card {

    public BilboFellowConspirator() {
        addEffect(EffectSlot.STATIC, new AddTreasureToFoodTokenCreationEffect());
    }
}
