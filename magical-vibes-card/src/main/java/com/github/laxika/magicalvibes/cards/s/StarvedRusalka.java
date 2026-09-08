package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "97")
public class StarvedRusalka extends Card {

    public StarvedRusalka() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new SacrificeCreatureCost(), new GainLifeEffect(1)),
                "{G}, Sacrifice a creature: You gain 1 life."
        ));
    }
}
