package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawAndRandomDiscardWithSharedTypeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "159")
public class RowdyCrew extends Card {

    public RowdyCrew() {
        // When Rowdy Crew enters the battlefield, draw three cards, then discard two cards at random.
        // If two cards that share a card type are discarded this way, put two +1/+1 counters on Rowdy Crew.
        // The draw is its own effect so the discard waits for any draw-replacement choices to finish.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(3));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawAndRandomDiscardWithSharedTypeCountersEffect(0, 2, 2));
    }
}
