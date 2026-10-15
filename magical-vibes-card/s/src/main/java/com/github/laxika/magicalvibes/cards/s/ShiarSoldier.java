package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "637")
public class ShiarSoldier extends Card {

    public ShiarSoldier() {
        // {U}, {T}: Return another target permanent you control to its owner's hand.
        // Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(ReturnToHandEffect.target()),
                "{U}, {T}: Return another target permanent you control to its owner's hand. "
                        + "Activate only during your turn.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                        "Target must be another permanent you control"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
