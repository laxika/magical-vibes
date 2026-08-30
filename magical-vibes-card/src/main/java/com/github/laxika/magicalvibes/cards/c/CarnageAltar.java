package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "198")
public class CarnageAltar extends Card {

    public CarnageAltar() {
        // {3}, Sacrifice a creature: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new SacrificeCreatureCost(), new DrawCardEffect()),
                "{3}, Sacrifice a creature: Draw a card."
        ));
    }
}
