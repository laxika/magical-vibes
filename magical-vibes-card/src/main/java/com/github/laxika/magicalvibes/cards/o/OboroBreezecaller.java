package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "48")
public class OboroBreezecaller extends Card {

    public OboroBreezecaller() {
        // {2}, Return a land you control to its owner's hand: Untap target land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsLandPredicate())),
                "{2}, Return a land you control to its owner's hand: Untap target land.",
                TargetFilters.land()));
    }
}
