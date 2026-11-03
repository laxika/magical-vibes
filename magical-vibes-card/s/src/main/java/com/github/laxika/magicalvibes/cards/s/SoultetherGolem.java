package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "164")
public class SoultetherGolem extends Card {

    public SoultetherGolem() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(1)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.TIME));
    }
}
