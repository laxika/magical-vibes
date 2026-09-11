package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "124")
public class VampiricRites extends Card {

    public VampiricRites() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeCreatureCost(), new GainLifeEffect(1), new DrawCardEffect(1)),
                "{1}{B}, Sacrifice a creature: You gain 1 life and draw a card."
        ));
    }
}
