package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "87")
public class CarrionLocust extends Card {

    public CarrionLocust() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardWithConditionalBonusEffect.creatureCardOwnerLosesLife(1));
    }
}
