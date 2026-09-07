package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "215")
public class ScrybRanger extends Card {

    public ScrybRanger() {
        // Return a Forest you control to its owner's hand: Untap target creature.
        // Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ReturnMultiplePermanentsToHandCost(
                                1, new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                        new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "Return a Forest you control to its owner's hand: Untap target creature. "
                        + "Activate only once each turn.",
                TargetFilters.creature(),
                null,
                1,
                null));
    }
}
