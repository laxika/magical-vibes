package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "41")
@CardRegistration(set = "MAT", collectorNumber = "91")
@CardRegistration(set = "MAT", collectorNumber = "141")
@CardRegistration(set = "MAT", collectorNumber = "176")
@CardRegistration(set = "MAT", collectorNumber = "220")
public class ObNixilisCaptiveKingpin extends Card {

    public ObNixilisCaptiveKingpin() {
        // "Whenever one or more opponents each lose exactly 1 life": the life lost is the event's value.
        addTrigger(Triggers.opponentLosesLife(),
                TriggerOptions.DEFAULT.withEventCondition(new AllOf(List.of(
                        new EventValueAtLeast(1), new NotCondition(new EventValueAtLeast(2))))),
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new ExileTopCardsMayPlayUntilNextEndStepEffect(1)));
    }
}
