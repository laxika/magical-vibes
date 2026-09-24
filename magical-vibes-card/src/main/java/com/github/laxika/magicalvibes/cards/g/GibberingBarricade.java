package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "95")
public class GibberingBarricade extends Card {

    public GibberingBarricade() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificeCreatureCost(),
                        new GainLifeEffect(1),
                        new DrawCardEffect(1)
                ),
                "{2}{B}, Sacrifice a creature: You gain 1 life and draw a card."
        ));
    }
}
