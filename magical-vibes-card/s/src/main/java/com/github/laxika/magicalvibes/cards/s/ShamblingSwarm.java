package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TOR", collectorNumber = "82")
public class ShamblingSwarm extends Card {

    public ShamblingSwarm() {
        target(TargetFilters.creature(), 1, 3)
                .addEffect(EffectSlot.ON_DEATH,
                        DistributeCountersAmongTargetsEffect.chosenUntilNextEndStep(
                                CounterType.MINUS_ONE_MINUS_ONE, 3));
    }
}
