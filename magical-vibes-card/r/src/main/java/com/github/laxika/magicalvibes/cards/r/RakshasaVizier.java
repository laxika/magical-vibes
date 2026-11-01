package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "193")
public class RakshasaVizier extends Card {

    public RakshasaVizier() {
        addTrigger(new TriggerCondition(Triggers.exiledFrom(Set.of(Zone.GRAVEYARD),
                null, ObjectRelation.YOU_OWN)), TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
