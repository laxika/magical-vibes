package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "50")
public class WaterspoutDjinn extends Card {

    private static final PermanentAllOfPredicate UNTAPPED_ISLAND = new PermanentAllOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
            new PermanentNotPredicate(new PermanentIsTappedPredicate())));

    public WaterspoutDjinn() {
        // Flying is a printed keyword loaded from Scryfall.
        // At the beginning of your upkeep, sacrifice this creature unless you return an untapped
        // Island you control to its owner's hand.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new ReturnMultiplePermanentsToHandCost(1, UNTAPPED_ISLAND),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
