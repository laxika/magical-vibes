package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "1")
@CardRegistration(set = "EMA", collectorNumber = "1")
@CardRegistration(set = "TSR", collectorNumber = "6")
public class AvenRiftwatcher extends Card {

    public AvenRiftwatcher() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                new RemoveCounterFromSourceEffect(CounterType.TIME, 1));
        addTrigger(Triggers.timeCountersRemovedFromThis(),
                new ConditionalEffect(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                        new SacrificeSelfEffect()));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new GainLifeEffect(2));
    }
}
