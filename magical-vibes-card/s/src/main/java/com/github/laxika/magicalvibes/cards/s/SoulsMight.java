package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALA", collectorNumber = "149")
public class SoulsMight extends Card {

    public SoulsMight() {
        target(TargetFilters.creature(), 1, 1)
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new TargetPower()));
    }
}
