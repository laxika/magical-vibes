package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "89")
public class BlightRot extends Card {

    public BlightRot() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, 4));
    }
}
