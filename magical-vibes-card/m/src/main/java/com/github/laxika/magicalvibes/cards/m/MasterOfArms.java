package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "20")
public class MasterOfArms extends Card {

    public MasterOfArms() {
        // {1}{W}: Tap target creature blocking this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}{W}: Tap target creature blocking this creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentBlockingSourcePredicate()
                        )),
                        "Target must be a creature blocking this creature"
                )
        ));
    }
}
