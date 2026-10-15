package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "210")
public class RumblingCrescendo extends Card {

    public RumblingCrescendo() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.VERSE),
                "Put a verse counter on Rumbling Crescendo?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new DestroyEachTargetPermanentEffect()),
                "{R}, Sacrifice this enchantment: Destroy up to X target lands, where X is the number of verse counters on this enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Targets must be lands"
                ),
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withSourceCounterScaledTargets(CounterType.VERSE));
    }
}
