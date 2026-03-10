package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "85")
public class BloodthroneVampire extends Card {

    public BloodthroneVampire() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new BoostSelfEffect(2, 2)),
                "Sacrifice a creature: Bloodthrone Vampire gets +2/+2 until end of turn.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));
    }
}
