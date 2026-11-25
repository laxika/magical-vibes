package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "233")
public class RisonaAsariCommander extends Card {

    public RisonaAsariCommander() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new SourceCounterThreshold(1, CounterType.INDESTRUCTIBLE))),
                PutCountersEffect.onSelf(CounterType.INDESTRUCTIBLE));

        addTrigger(Triggers.youAreDealtCombatDamage(),
                RemoveCountersEffect.fromSelf(CounterType.INDESTRUCTIBLE, 1));
    }
}
