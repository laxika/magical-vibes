package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "17")
public class ScepterOfDominance extends Card {

    public ScepterOfDominance() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{W}, {T}: Tap target permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent"
                )
        ));
    }
}
