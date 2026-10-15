package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "151")
public class WallOfCorpses extends Card {

    public WallOfCorpses() {
        // {B}, Sacrifice Wall of Corpses: Destroy target creature this creature is blocking.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect(false)),
                "{B}, Sacrifice Wall of Corpses: Destroy target creature this creature is blocking.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentBlockedBySourcePredicate()
                        )),
                        "Target must be a creature this creature is blocking"
                )
        ));
    }
}
