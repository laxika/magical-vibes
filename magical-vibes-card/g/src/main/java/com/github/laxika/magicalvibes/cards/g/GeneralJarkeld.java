package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SwapExclusiveBlockersBetweenTwoBlockedAttackersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "27")
public class GeneralJarkeld extends Card {

    public GeneralJarkeld() {
        // {T}: Choose two target blocked attacking creatures. If each of those creatures could be
        // blocked by all creatures that the other is blocked by, each creature that's blocking
        // exactly one of those attacking creatures stops blocking it and is blocking the other
        // attacking creature. Activate only during the declare blockers step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SwapExclusiveBlockersBetweenTwoBlockedAttackersEffect()),
                "{T}: Choose two target blocked attacking creatures. If each of those creatures could "
                        + "be blocked by all creatures that the other is blocked by, each creature that's "
                        + "blocking exactly one of those attacking creatures stops blocking it and is "
                        + "blocking the other attacking creature. Activate only during the declare blockers step.",
                null,
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_DECLARE_BLOCKERS,
                List.of(
                        new PermanentPredicateTargetFilter(
                                new PermanentIsBlockedPredicate(),
                                "Target must be a blocked attacking creature"),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsBlockedPredicate(),
                                "Target must be a blocked attacking creature")
                ),
                2,
                2
        ));
    }
}
