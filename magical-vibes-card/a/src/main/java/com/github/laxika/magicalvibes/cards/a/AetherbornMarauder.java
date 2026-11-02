package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromControlledPermanentsToSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "71")
@CardRegistration(set = "KLR", collectorNumber = "76")
@CardRegistration(set = "OTC", collectorNumber = "125")
public class AetherbornMarauder extends Card {

    public AetherbornMarauder() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MoveCountersFromControlledPermanentsToSourceEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
