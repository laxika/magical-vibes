package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "227")
public class TheFiligreeSylex extends Card {

    public TheFiligreeSylex() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.OIL)),
                "{T}: Put an oil counter on The Filigree Sylex."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentManaValueEqualsSourceCountersPredicate(CounterType.OIL)
                        )))
                ),
                "{T}, Sacrifice The Filigree Sylex: Destroy each nonland permanent with mana value "
                        + "equal to the number of oil counters on The Filigree Sylex."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromControlledPermanentCost(List.of(CounterType.OIL), 10, null, false),
                        new SacrificeSelfCost(),
                        new DealDamageToAnyTargetEffect(10)
                ),
                "{T}, Remove ten oil counters from among permanents you control and sacrifice The Filigree "
                        + "Sylex: It deals 10 damage to any target."
        ));
    }
}
