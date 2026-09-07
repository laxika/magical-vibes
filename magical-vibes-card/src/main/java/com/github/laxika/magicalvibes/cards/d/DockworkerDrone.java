package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "11")
public class DockworkerDrone extends Card {

    public DockworkerDrone() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_DEATH,
                new PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
