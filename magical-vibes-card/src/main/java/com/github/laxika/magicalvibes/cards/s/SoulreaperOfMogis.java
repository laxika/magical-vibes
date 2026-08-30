package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "115")
public class SoulreaperOfMogis extends Card {

    public SoulreaperOfMogis() {
        // {2}{B}, Sacrifice a creature: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new SacrificeCreatureCost(), new DrawCardEffect()),
                "{2}{B}, Sacrifice a creature: Draw a card.",
                TargetFilters.creatureYouControl()
        ));
    }
}
