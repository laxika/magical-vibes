package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandCardWithManaValueAndCastEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "177")
@CardRegistration(set = "A25", collectorNumber = "235")
@CardRegistration(set = "2XM", collectorNumber = "302")
public class TreasureKeeper extends Card {

    public TreasureKeeper() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new RevealUntilNonlandCardWithManaValueAndCastEffect(3));
    }
}
