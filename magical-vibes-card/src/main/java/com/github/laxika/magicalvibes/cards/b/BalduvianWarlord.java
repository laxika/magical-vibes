package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BalduvianWarlordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "77")
public class BalduvianWarlord extends Card {

    private static final PermanentAllOfPredicate BLOCKING_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsBlockingPredicate()
    ));

    public BalduvianWarlord() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BalduvianWarlordEffect()),
                "{T}: Remove target blocking creature from combat. Creatures it was blocking that hadn't "
                        + "become blocked by another creature this combat become unblocked, then it blocks "
                        + "an attacking creature of your choice. Activate only during the declare blockers step.",
                new PermanentPredicateTargetFilter(BLOCKING_CREATURE, "Target must be a blocking creature"),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_DECLARE_BLOCKERS
        ));
    }
}
