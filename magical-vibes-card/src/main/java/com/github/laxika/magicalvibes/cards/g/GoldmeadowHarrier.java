package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "17")
public class GoldmeadowHarrier extends Card {

    public GoldmeadowHarrier() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{W}, {T}: Tap target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
