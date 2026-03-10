package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "122")
public class AncientHellkite extends Card {

    public AncientHellkite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{R}: Ancient Hellkite deals 1 damage to target creature defending player controls. Activate only if Ancient Hellkite is attacking.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                        )),
                        "Target must be a creature defending player controls"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_WHILE_ATTACKING
        ));
    }
}
