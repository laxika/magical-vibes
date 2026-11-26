package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "123")
public class HeightenedReflexes extends Card {

    public HeightenedReflexes() {
        // Target creature gets +1/+0 until end of turn. Put a first strike counter on it.
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 0))
                .addSpellEffect(new PutCountersEffect(CounterType.FIRST_STRIKE, 1));
    }
}
