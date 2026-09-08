package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "135")
public class SinisterConcoction extends Card {

    public SinisterConcoction() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new PayLifeCost(1),
                        new MillControllerCost(1),
                        new DiscardCardTypeCost(null, null),
                        new SacrificeSelfCost(),
                        new DestroyTargetPermanentEffect()
                ),
                "{B}, Pay 1 life, Mill a card, Discard a card, Sacrifice this enchantment: Destroy target creature.",
                TargetFilters.creature()
        ));
    }
}
