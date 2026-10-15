package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "127")
public class TrenchWurm extends Card {

    public TrenchWurm() {
        // {2}{R}, {T}: Destroy target nonbasic land.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new DestroyTargetPermanentEffect(false)),
                "{2}{R}, {T}: Destroy target nonbasic land.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                        )),
                        "Target must be a nonbasic land"
                )
        ));
    }
}
