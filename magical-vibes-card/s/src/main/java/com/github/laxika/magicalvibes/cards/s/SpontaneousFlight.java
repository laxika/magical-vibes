package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "33")
public class SpontaneousFlight extends Card {

    public SpontaneousFlight() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2))
                .addSpellEffect(new PutCountersEffect(CounterType.FLYING));
    }
}
