package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromControlledPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "85")
public class RetributionOfTheAncients extends Card {

    public RetributionOfTheAncients() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new RemoveOneOrMoreCountersFromControlledPermanentsCost(CounterType.PLUS_ONE_PLUS_ONE,
                                new PermanentIsCreaturePredicate(), true, false),
                        BoostEffect.target(
                                new Scaled(new XValue(), -1),
                                new Scaled(new XValue(), -1))
                ),
                "{B}, Remove X +1/+1 counters from among creatures you control: Target creature gets -X/-X until end of turn.",
                TargetFilters.creature()
        ).withXValueFromControlledCreatureCounters());
    }
}
