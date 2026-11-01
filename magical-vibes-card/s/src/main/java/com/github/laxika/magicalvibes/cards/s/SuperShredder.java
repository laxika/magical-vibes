package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "83")
@CardRegistration(set = "TMT", collectorNumber = "217")
@CardRegistration(set = "TMT", collectorNumber = "285")
@CardRegistration(set = "TMT", collectorNumber = "295")
public class SuperShredder extends Card {

    public SuperShredder() {
        // Whenever another permanent leaves the battlefield, put a +1/+1 counter on Super Shredder.
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
