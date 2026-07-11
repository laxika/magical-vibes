package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PTK", collectorNumber = "84")
public class StoneCatapult extends Card {

    public StoneCatapult() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect(false)),
                "{T}: Destroy target tapped nonblack creature. Activate only during your turn, before attackers are declared.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate(),
                                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                        )),
                        "Target must be a tapped nonblack creature"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED
        ));
    }
}
