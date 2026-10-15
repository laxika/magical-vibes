package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "80")
public class WallOfVipers extends Card {

    public WallOfVipers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new DestroyReferencedPermanentEffect(PermanentReference.SOURCE),
                        new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentBlockedBySourcePredicate()
                        )))
                ),
                "{3}: Destroy Wall of Vipers and target creature it's blocking. Any player may activate this ability.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentBlockedBySourcePredicate()
                        )),
                        "Target must be a creature this creature is blocking"
                )
        ).withActivatableByAnyPlayer());
    }
}
