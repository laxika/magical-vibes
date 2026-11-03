package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "23")
@CardRegistration(set = "EMA", collectorNumber = "5")
@CardRegistration(set = "TSR", collectorNumber = "11")
public class Calciderm extends Card {

    public Calciderm() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(4)));
        // Vanishing: the upkeep removal and the last-counter sacrifice are separate triggers
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                new RemoveCounterFromSourceEffect(CounterType.TIME, 1));
        addTrigger(Triggers.timeCountersRemovedFromThis(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME))),
                new SacrificeSelfEffect());
    }
}
