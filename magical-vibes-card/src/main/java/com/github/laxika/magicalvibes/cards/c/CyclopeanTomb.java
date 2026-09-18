package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutMireCounterOnTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterCyclopeanTombMireCleanupEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ME4", collectorNumber = "195")
public class CyclopeanTomb extends Card {

    private static final PermanentAllOfPredicate NON_SWAMP_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SWAMP))));

    public CyclopeanTomb() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantEffectsToCounterBearersEffect(CounterType.MIRE,
                List.of(new GrantSubtypeEffect(CardSubtype.SWAMP, GrantScope.ALL_LANDS, true))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new PutMireCounterOnTargetLandEffect()),
                "{2}, {T}: Put a mire counter on target non-Swamp land. Activate only during your upkeep.",
                new PermanentPredicateTargetFilter(NON_SWAMP_LAND, "Target must be a non-Swamp land"),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP));

        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new RegisterCyclopeanTombMireCleanupEffect());
    }
}
