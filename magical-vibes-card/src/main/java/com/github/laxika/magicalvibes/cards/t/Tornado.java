package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "101")
public class Tornado extends Card {

    public Tornado() {
        // Cumulative upkeep {G}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{G}"));

        // {2}{G}, Pay 3 life for each velocity counter on this enchantment: Destroy target
        // permanent and put a velocity counter on this enchantment. Activate only once each turn.
        // The life cost is sized from the counters present as the ability is activated, so the
        // first activation each game costs no life.
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(PayLifeCost.perSourceCounter(3, CounterType.VELOCITY),
                        new DestroyTargetPermanentEffect(false),
                        new PutCountersOnSelfEffect(CounterType.VELOCITY)),
                "{2}{G}, Pay 3 life for each velocity counter on this enchantment: Destroy target "
                        + "permanent and put a velocity counter on this enchantment. "
                        + "Activate only once each turn.",
                new PermanentPredicateTargetFilter(new PermanentTruePredicate(),
                        "Target must be a permanent"),
                null, 1, null));
    }
}
