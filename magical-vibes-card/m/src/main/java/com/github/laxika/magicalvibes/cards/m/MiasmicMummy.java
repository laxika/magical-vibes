package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "100")
@CardRegistration(set = "AKR", collectorNumber = "116")
public class MiasmicMummy extends Card {

    public MiasmicMummy() {
        // When this creature enters, each player discards a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));
    }
}
