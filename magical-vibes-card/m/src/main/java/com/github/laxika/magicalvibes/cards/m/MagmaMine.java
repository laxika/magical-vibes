package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "149")
public class MagmaMine extends Card {

    public MagmaMine() {
        // {4}: Put a pressure counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new PutCountersOnSelfEffect(CounterType.PRESSURE)),
                "{4}: Put a pressure counter on this artifact."
        ));

        // {T}, Sacrifice this artifact: It deals damage equal to the number of pressure counters
        // on it to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.PRESSURE))
                ),
                "{T}, Sacrifice this artifact: It deals damage equal to the number of pressure counters on it to any target."
        ));
    }
}
