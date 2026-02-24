package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "148")
public class CullingDais extends Card {

    public CullingDais() {
        // {T}, Sacrifice a creature: Put a charge counter on Culling Dais.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeCreatureCost(), new PutChargeCounterOnSelfEffect()),
                "{T}, Sacrifice a creature: Put a charge counter on Culling Dais.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));

        // {1}, Sacrifice Culling Dais: Draw a card for each charge counter on Culling Dais.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new DrawCardsEqualToChargeCountersOnSourceEffect()),
                "{1}, Sacrifice Culling Dais: Draw a card for each charge counter on Culling Dais."
        ));
    }
}
