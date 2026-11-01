package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "53")
public class BroodOfCockroaches extends Card {

    public BroodOfCockroaches() {
        // When this creature is put into your graveyard from the battlefield, at the beginning of
        // the next end step, you lose 1 life and return this card to your hand.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect(1));
    }
}
