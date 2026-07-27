package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "337")
@CardRegistration(set = "6ED", collectorNumber = "307")
public class PhyrexianVault extends Card {

    public PhyrexianVault() {
        // {2}, {T}, Sacrifice a creature: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeCreatureCost(), new DrawCardEffect()),
                "{2}, {T}, Sacrifice a creature: Draw a card.",
                TargetFilters.creatureYouControl()
        ));
    }
}
