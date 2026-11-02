package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "86")
@CardRegistration(set = "RIX", collectorNumber = "53")
public class SilvergillAdept extends Card {

    public SilvergillAdept() {
        // As an additional cost to cast this spell, reveal a Merfolk card from your hand or pay {3}.
        addStaticEffect(new IncreaseOwnCastCostUnlessRevealSubtypeEffect(3, CardSubtype.MERFOLK));

        // When this creature enters, draw a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
