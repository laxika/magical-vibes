package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "34")
public class Floodbringer extends Card {

    public Floodbringer() {
        // {2}, Return a land you control to its owner's hand: Tap target land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}, Return a land you control to its owner's hand: Tap target land.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land"
                )
        ));
    }
}
