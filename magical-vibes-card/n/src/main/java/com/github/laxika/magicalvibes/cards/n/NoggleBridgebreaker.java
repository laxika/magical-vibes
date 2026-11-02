package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "107")
public class NoggleBridgebreaker extends Card {

    public NoggleBridgebreaker() {
        // When this creature enters, return a land you control to its owner's hand. The land is chosen
        // on resolution rather than targeted, so the trigger still goes on the stack with no lands.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnPermanentControlledByPlayerToHandEffect(new PermanentIsLandPredicate(), "land"));
    }
}
