package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "156")
public class SerratedBiskelion extends Card {

    public SerratedBiskelion() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCountersOnSourceEffect(-1, -1, 1),
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)
                ),
                "{T}: Put a -1/-1 counter on this creature and a -1/-1 counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
