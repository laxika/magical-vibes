package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "67")
@CardRegistration(set = "ONC", collectorNumber = "93")
public class IchorRats extends Card {

    public IchorRats() {
        // When Ichor Rats enters the battlefield, each player gets a poison counter.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GivePoisonCountersEffect(1, PoisonRecipient.EACH_PLAYER));
    }
}
