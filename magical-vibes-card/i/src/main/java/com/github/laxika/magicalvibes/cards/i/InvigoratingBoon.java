package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "267")
@CardRegistration(set = "DMR", collectorNumber = "164")
public class InvigoratingBoon extends Card {

    private static final String PROMPT = "Put a +1/+1 counter on target creature?";

    public InvigoratingBoon() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.playerCyclesCard(), new MayEffect(
                        new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1), PROMPT));
    }
}
