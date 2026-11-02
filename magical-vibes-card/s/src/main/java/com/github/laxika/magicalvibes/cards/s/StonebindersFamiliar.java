package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "31")
public class StonebindersFamiliar extends Card {

    public StonebindersFamiliar() {
        addTrigger(new TriggerCondition(Triggers.exiledFrom(
                                Set.of(),
                                Triggers.NONTOKEN,
                                ObjectRelation.ANY).onlyDuringYourTurn()),
                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
