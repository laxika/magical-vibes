package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GiveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "44")
@CardRegistration(set = "PIP", collectorNumber = "572")
@CardRegistration(set = "PIP", collectorNumber = "381")
@CardRegistration(set = "PIP", collectorNumber = "909")
public class FeralGhoul extends Card {

    public FeralGhoul() {
        // Whenever another creature you control dies, put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        // When this creature dies, each opponent gets rad counters equal to its power.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new GiveRadCountersEffect(new EventValue(), Subjects.eachOpponent()));
    }
}
