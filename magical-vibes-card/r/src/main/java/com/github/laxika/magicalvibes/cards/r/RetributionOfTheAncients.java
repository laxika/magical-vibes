package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromControlledCreaturesCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "85")
public class RetributionOfTheAncients extends Card {

    public RetributionOfTheAncients() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new RemoveOneOrMoreCountersFromControlledCreaturesCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new BoostTargetCreatureEffect(
                                new Scaled(new XValue(), -1),
                                new Scaled(new XValue(), -1))
                ),
                "{B}, Remove X +1/+1 counters from among creatures you control: Target creature gets -X/-X until end of turn.",
                TargetFilters.creature()
        ).withXValueFromControlledCreatureCounters());
    }
}
