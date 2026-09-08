package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "77")
public class BushmeatPoacher extends Card {

    public BushmeatPoacher() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, true, true),
                        new GainLifeEffect(new XValue()),
                        new DrawCardEffect()
                ),
                "{1}, {T}, Sacrifice another creature: You gain life equal to the sacrificed creature's toughness. Draw a card."
        ));
    }
}
