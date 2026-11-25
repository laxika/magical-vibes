package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "46")
public class SereneSteward extends Card {

    public SereneSteward() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.youGainLife(), UnlessPaysEffect.youMayPay("{W}", new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE))
                        .withPrompt("Pay {W} to put a +1/+1 counter on target creature?"));
    }
}
