package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "144")
public class GristleGlutton extends Card {

    public GristleGlutton() {
        // "{T}, Blight 1: Discard a card. If you do, draw a card."
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BlightEffect(1, new DiscardCardThenEffect(null, new DrawCardEffect(1), "a card"))),
                "{T}, Blight 1: Discard a card. If you do, draw a card."
        ));
    }
}
