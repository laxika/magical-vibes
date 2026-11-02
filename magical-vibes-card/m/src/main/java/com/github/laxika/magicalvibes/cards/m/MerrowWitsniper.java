package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "40")
public class MerrowWitsniper extends Card {

    public MerrowWitsniper() {
        // When this creature enters, target player mills a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(1, MillRecipient.TARGET_PLAYER));
    }
}
