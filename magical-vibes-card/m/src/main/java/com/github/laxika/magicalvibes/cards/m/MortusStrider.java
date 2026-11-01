package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "179")
@CardRegistration(set = "RVR", collectorNumber = "203")
public class MortusStrider extends Card {

    public MortusStrider() {
        // When this creature dies, return it to its owner's hand.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
