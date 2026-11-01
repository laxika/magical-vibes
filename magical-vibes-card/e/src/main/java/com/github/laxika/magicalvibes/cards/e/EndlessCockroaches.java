package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "92")
@CardRegistration(set = "C13", collectorNumber = "75")
public class EndlessCockroaches extends Card {

    public EndlessCockroaches() {
        // When this creature dies, return it to its owner's hand.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
