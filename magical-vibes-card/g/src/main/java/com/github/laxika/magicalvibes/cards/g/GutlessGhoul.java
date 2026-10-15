package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "60")
public class GutlessGhoul extends Card {

    public GutlessGhoul() {
        // {1}, Sacrifice a creature: You gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeCreatureCost(), new GainLifeEffect(2)),
                "{1}, Sacrifice a creature: You gain 2 life."
        ));
    }
}
