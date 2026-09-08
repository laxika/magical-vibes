package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "146")
@CardRegistration(set = "SOI", collectorNumber = "171")
public class MadProphet extends Card {

    public MadProphet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DrawCardEffect(1)
                ),
                "{T}, Discard a card: Draw a card."
        ));
    }
}
