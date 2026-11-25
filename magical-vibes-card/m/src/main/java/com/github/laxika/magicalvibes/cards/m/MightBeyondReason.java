package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "217")
public class MightBeyondReason extends Card {

    public MightBeyondReason() {
        target(TargetFilters.creature()).addSpellEffect(new ConditionalReplacementEffect(
                new Delirium(),
                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)
        ));
    }
}
