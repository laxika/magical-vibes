package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;

@CardRegistration(set = "BRO", collectorNumber = "87")
public class CarrionLocust extends Card {

    public CarrionLocust() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ExileGraveyardCardWithConditionalBonusEffect.creatureCardOwnerLosesLife(1));
    }
}
