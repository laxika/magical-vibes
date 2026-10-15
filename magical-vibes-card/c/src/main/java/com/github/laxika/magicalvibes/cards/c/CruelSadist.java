package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "93")
public class CruelSadist extends Card {

    public CruelSadist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new PayLifeCost(1), new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{B}, {T}, Pay 1 life: Put a +1/+1 counter on this creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new DealDamageToTargetCreatureEffect(new XValue())
                ),
                "{2}{B}, {T}, Remove X +1/+1 counters from this creature: It deals X damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
