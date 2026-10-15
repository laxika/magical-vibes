package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "79")
public class UnspeakableSymbol extends Card {

    public UnspeakableSymbol() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayLifeCost(3),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)
                ),
                "Pay 3 life: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
