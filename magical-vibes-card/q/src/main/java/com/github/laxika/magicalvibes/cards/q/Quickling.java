package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "76")
@CardRegistration(set = "WOC", collectorNumber = "103")
public class Quickling extends Card {

    public Quickling() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.CREATURE, true));
    }
}
