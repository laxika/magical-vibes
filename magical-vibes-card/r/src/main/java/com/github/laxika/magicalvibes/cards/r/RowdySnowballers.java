package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "68")
public class RowdySnowballers extends Card {

    public RowdySnowballers() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(Subjects.target()),
                        new PutCountersEffect(CounterType.STUN));
    }
}
