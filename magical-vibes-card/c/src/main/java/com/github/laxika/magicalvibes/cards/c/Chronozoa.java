package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "37")
public class Chronozoa extends Card {

    public Chronozoa() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                new RemoveCounterFromSourceEffect(CounterType.TIME, 1));
        addTrigger(Triggers.timeCountersRemovedFromThis(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME))),
                new SacrificeSelfEffect());
        addTrigger(Triggers.dies(
                        new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.TIME)),
                        ObjectRelation.THIS),
                new CreateTokenCopyOfSourceEffect(false, 2));
    }
}
