package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "132")
@CardRegistration(set = "7ED", collectorNumber = "136")
@CardRegistration(set = "STH", collectorNumber = "59")
@CardRegistration(set = "DDC", collectorNumber = "32")
@CardRegistration(set = "DVD", collectorNumber = "32")
@CardRegistration(set = "DDR", collectorNumber = "49")
public class FoulImp extends Card {

    public FoulImp() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(2));
    }
}
