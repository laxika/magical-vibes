package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "93")
public class XunYuWeiAdvisor extends Card {

    public XunYuWeiAdvisor() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new BoostTargetCreatureEffect(2, 0)),
                "{T}: Target creature you control gets +2/+0 until end of turn. Activate only during your turn, before attackers are declared.",
                new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"),
                null, null, ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED));
    }
}
