package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "37")
public class MentalDiscipline extends Card {

    public MentalDiscipline() {
        // {1}{U}, Discard a card: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect(1)),
                "{1}{U}, Discard a card: Draw a card."
        ));
    }
}
