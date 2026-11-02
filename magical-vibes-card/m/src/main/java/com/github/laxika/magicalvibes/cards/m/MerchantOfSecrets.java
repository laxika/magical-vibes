package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "90")
@CardRegistration(set = "LGN", collectorNumber = "44")
@CardRegistration(set = "C17", collectorNumber = "87")
public class MerchantOfSecrets extends Card {

    public MerchantOfSecrets() {
        // When this creature enters, draw a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
    }
}
