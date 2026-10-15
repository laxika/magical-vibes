package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "32")
public class FloodedShoreline extends Card {

    public FloodedShoreline() {
        // {U}{U}, Return two Islands you control to their owner's hand:
        // Return target creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(
                        new ReturnMultiplePermanentsToHandCost(2, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                        ReturnToHandEffect.target()
                ),
                "{U}{U}, Return two Islands you control to their owner's hand: "
                        + "Return target creature to its owner's hand.",
                TargetFilters.creature()
        ));
    }
}
