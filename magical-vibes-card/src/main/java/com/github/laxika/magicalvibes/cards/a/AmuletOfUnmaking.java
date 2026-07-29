package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "293")
public class AmuletOfUnmaking extends Card {

    public AmuletOfUnmaking() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new ExileSelfCost(), new ExileTargetPermanentEffect()),
                "{5}, {T}, Exile this artifact: Exile target artifact, creature, or land. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate()
                        )),
                        "Target must be an artifact, creature, or land"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
