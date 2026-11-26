package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "154")
public class FullyGrown extends Card {

    public FullyGrown() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 3))
                .addSpellEffect(new PutCountersEffect(CounterType.TRAMPLE));
    }
}
