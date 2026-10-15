package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndRandomDiscardWithSharedTypeCountersEffect;

@CardRegistration(set = "XLN", collectorNumber = "159")
public class RowdyCrew extends Card {

    public RowdyCrew() {
        // When Rowdy Crew enters the battlefield, draw three cards, then discard two cards at random.
        // If two cards that share a card type are discarded this way, put two +1/+1 counters on Rowdy Crew.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawAndRandomDiscardWithSharedTypeCountersEffect(3, 2, 2));
    }
}
