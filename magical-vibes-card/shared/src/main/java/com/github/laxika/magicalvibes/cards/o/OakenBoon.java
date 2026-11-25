package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class OakenBoon extends Card {

    public OakenBoon() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
