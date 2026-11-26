package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "76")
public class BoneShards extends Card {

    public BoneShards() {
        // As an additional cost to cast this spell, sacrifice a creature or discard a card.
        // Destroy target creature or planeswalker.
        addCastingCost(AnyOfCost.of(new DiscardCardTypeCost(null, null),
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "a creature", false)));
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                "Target must be a creature or planeswalker"))
                .addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
