package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "140")
public class StripedBears extends Card {

    public StripedBears() {
        // When this creature enters, draw a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
