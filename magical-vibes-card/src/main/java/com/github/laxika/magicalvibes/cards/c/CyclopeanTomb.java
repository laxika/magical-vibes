package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterCyclopeanTombUpkeepCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.RememberMireCounterLandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "2ED", collectorNumber = "241")
@CardRegistration(set = "ME4", collectorNumber = "195")
public class CyclopeanTomb extends Card {

    public CyclopeanTomb() {
        var nonSwampLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SWAMP))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.MIRE),
                        new RememberMireCounterLandEffect(),
                        new GrantSubtypeToTargetWhileHasCounterEffect(CardSubtype.SWAMP, CounterType.MIRE, true)
                ),
                "{2}, {T}: Put a mire counter on target non-Swamp land. That land is a Swamp for as long as it has a mire counter on it. Activate only during your upkeep.",
                new PermanentPredicateTargetFilter(nonSwampLand, "Target must be a non-Swamp land"),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));

        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new RegisterCyclopeanTombUpkeepCleanupEffect());
    }
}
