package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdjustEachCounterKindOnTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "AKH", collectorNumber = "181")
public class QuarryHauler extends Card {

    public QuarryHauler() {
        target(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AdjustEachCounterKindOnTargetEffect());
    }
}
