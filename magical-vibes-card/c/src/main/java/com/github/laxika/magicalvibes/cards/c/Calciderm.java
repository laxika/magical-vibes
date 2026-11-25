package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "23")
@CardRegistration(set = "EMA", collectorNumber = "5")
@CardRegistration(set = "TSR", collectorNumber = "11")
public class Calciderm extends Card {

    public Calciderm() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(4)));
        // Vanishing's "when the last is removed, sacrifice it" is the keyword's own trigger (CR 702.63a).
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                RemoveCountersEffect.fromSelf(CounterType.TIME, 1));
    }
}
