package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "182")
public class DwarvenArmory extends Card {

    public DwarvenArmory() {
        // {2}, Sacrifice a land: Put a +2/+2 counter on target creature. Activate only during any upkeep step.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_TWO_PLUS_TWO, 1)
                ),
                "{2}, Sacrifice a land: Put a +2/+2 counter on target creature. Activate only during any upkeep step.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_ANY_UPKEEP
        ));
    }
}
