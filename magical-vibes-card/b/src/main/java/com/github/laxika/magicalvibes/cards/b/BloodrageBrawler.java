package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "121")
public class BloodrageBrawler extends Card {

    public BloodrageBrawler() {
        // When this creature enters, discard a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
