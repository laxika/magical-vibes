package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "129")
public class WitchsCauldron extends Card {

    public WitchsCauldron() {
        // {1}{B}, {T}, Sacrifice a creature: You gain 1 life and draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new SacrificeCreatureCost(), new GainLifeEffect(1), new DrawCardEffect()),
                "{1}{B}, {T}, Sacrifice a creature: You gain 1 life and draw a card."
        ));
    }
}
