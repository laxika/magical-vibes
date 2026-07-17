package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ALA", collectorNumber = "88")
public class SkeletalKathari extends Card {

    public SkeletalKathari() {
        // {B}, Sacrifice a creature: Regenerate Skeletal Kathari.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeCreatureCost(), new RegenerateEffect()),
                "{B}, Sacrifice a creature: Regenerate Skeletal Kathari.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));
    }
}
