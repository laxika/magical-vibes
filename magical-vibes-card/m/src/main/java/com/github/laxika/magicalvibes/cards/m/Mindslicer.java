package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "146")
@CardRegistration(set = "ODY", collectorNumber = "149")
@CardRegistration(set = "DMR", collectorNumber = "91")
public class Mindslicer extends Card {

    public Mindslicer() {
        // When this creature dies, each player discards their hand.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));
    }
}
