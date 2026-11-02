package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "1")
public class LongBodiedGreyDog extends Card {

    public LongBodiedGreyDog() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofTappedTreasureToken(1));
    }
}
