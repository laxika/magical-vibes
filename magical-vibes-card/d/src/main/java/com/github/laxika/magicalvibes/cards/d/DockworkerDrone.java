package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;

@CardRegistration(set = "EOE", collectorNumber = "11")
public class DockworkerDrone extends Card {

    public DockworkerDrone() {
        addEffect(EffectSlot.STATIC,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        addEffect(EffectSlot.ON_DEATH, MoveDyingSourceCountersToTargetCreatureEffect.alwaysTriggers(true));
    }
}
