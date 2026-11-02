package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "210")
public class RumblingCrescendo extends Card {

    public RumblingCrescendo() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
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
                Integer.MAX_VALUE
        ).withSourceCounterScaledTargets(CounterType.VERSE));
    }
}
