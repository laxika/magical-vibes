package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "19a")
@CardRegistration(set = "HML", collectorNumber = "19b")
public class TradeCaravan extends Card {

    public TradeCaravan() {
        // At the beginning of your upkeep, put a currency counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.CURRENCY));

        PermanentAllOfPredicate basicLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.BASIC)));

        // Remove two currency counters from this creature: Untap target basic land.
        // Activate only during an opponent's upkeep.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new RemoveCounterFromSourceCost(2, CounterType.CURRENCY),
                        new UntapPermanentsEffect(TapUntapScope.TARGET, basicLand)),
                "Remove two currency counters from this creature: Untap target basic land. "
                        + "Activate only during an opponent's upkeep.",
                new PermanentPredicateTargetFilter(basicLand, "basic land"),
                null, null, ActivationTimingRestriction.ONLY_DURING_OPPONENTS_UPKEEP));
    }
}
