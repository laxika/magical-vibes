package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUnlessPaysPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "85")
public class Musician extends Card {

    public Musician() {
        // Cumulative upkeep {1}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        // {T}: Put a music counter on target creature. If it doesn't have "At the beginning of your
        // upkeep, destroy this creature unless you pay {1} for each music counter on it," it gains
        // that ability. (Grant is idempotent — ability at most once; further activations add counters.)
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.MUSIC, 1),
                        new GrantEffectToTargetEffect(
                                EffectSlot.UPKEEP_TRIGGERED,
                                new DestroyUnlessPaysPerCounterEffect(CounterType.MUSIC, "{1}"))
                ),
                "{T}: Put a music counter on target creature. If it doesn't have \"At the beginning "
                        + "of your upkeep, destroy this creature unless you pay {1} for each music "
                        + "counter on it,\" it gains that ability.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
