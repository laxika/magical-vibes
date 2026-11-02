package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "65")
@CardRegistration(set = "ECC", collectorNumber = "73")
public class DuskUrchins extends Card {

    public DuskUrchins() {
        // Whenever this creature attacks or blocks, put a -1/-1 counter on it.
        addTrigger(Triggers.attacks(), new PutCountersOnSourceEffect(-1, -1, 1));
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new PutCountersOnSourceEffect(-1, -1, 1));

        // When this creature dies, draw a card for each -1/-1 counter on it.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new DrawCardForEachDyingSourceCounterEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
