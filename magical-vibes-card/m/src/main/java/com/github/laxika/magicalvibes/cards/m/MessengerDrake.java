package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M14", collectorNumber = "63")
public class MessengerDrake extends Card {

    public MessengerDrake() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
