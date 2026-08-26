package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachLeavingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPM", collectorNumber = "12")
public class SelflessPoliceCaptain extends Card {

    public SelflessPoliceCaptain() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new PutCounterOnTargetForEachLeavingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
