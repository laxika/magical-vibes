package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "66")
public class CarrionImp extends Card {

    public CarrionImp() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(
                        ExileGraveyardCardWithConditionalBonusEffect.creatureCardOnly(2),
                        "Exile target creature card from a graveyard?"));
    }
}
