package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "12")
public class GirdForBattle extends Card {

    public GirdForBattle() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
