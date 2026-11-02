package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "233")
public class RisonaAsariCommander extends Card {

    public RisonaAsariCommander() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.INDESTRUCTIBLE)),
                new PutCountersOnSelfEffect(CounterType.INDESTRUCTIBLE)));

        addTrigger(Triggers.youAreDealtCombatDamage(),
                new RemoveCounterFromSourceEffect(CounterType.INDESTRUCTIBLE, 1));
    }
}
