package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "231")
public class NathsElite extends Card {

    public NathsElite() {
        // All creatures able to block this creature do so.
        addStaticEffect(new MustBeBlockedByAllCreaturesEffect());

        // When this creature enters, clash with an opponent. If you win, put a +1/+1 counter on it.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ClashEffect(new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
