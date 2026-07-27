package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "156")
public class GrimBackwoods extends Card {

    public GrimBackwoods() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}{B}{G}, {T}, Sacrifice a creature: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}{G}",
                List.of(new SacrificeCreatureCost(), new DrawCardEffect(1)),
                "{2}{B}{G}, {T}, Sacrifice a creature: Draw a card.",
                TargetFilters.creatureYouControl()
        ));
    }
}
