package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YNEO", collectorNumber = "21")
public class BellowsbreathOgre extends Card {

    public BellowsbreathOgre() {
        // Starting intensity 1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.INTENSITY));

        // Whenever this creature attacks, it deals damage equal to its intensity to any target.
        // Then this creature intensifies by 1.
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.INTENSITY)),
                new PutCountersOnSelfEffect(CounterType.INTENSITY)));
    }
}
