package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "143")
public class RiverHoopoe extends Card {

    public RiverHoopoe() {
        // Flying (keyword loaded from Scryfall)
        // {3}{G}{U}: You gain 2 life and draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{U}",
                List.of(new GainLifeEffect(2), new DrawCardEffect(1)),
                "{3}{G}{U}: You gain 2 life and draw a card."));
    }
}
