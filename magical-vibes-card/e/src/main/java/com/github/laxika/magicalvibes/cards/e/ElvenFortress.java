package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "65a")
@CardRegistration(set = "FEM", collectorNumber = "65b")
@CardRegistration(set = "FEM", collectorNumber = "65c")
@CardRegistration(set = "FEM", collectorNumber = "65d")
public class ElvenFortress extends Card {

    private static final PermanentAllOfPredicate BLOCKING_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsBlockingPredicate()
    ));

    public ElvenFortress() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new BoostTargetCreatureEffect(0, 1, BLOCKING_CREATURE)),
                "{1}{G}: Target blocking creature gets +0/+1 until end of turn.",
                new PermanentPredicateTargetFilter(BLOCKING_CREATURE, "Target must be a blocking creature")
        ));
    }
}
