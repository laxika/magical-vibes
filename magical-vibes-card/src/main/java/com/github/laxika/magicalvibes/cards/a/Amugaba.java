package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "61")
public class Amugaba extends Card {

    public Amugaba() {
        // {2}{U}, Discard a card: Return this creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new DiscardCardTypeCost(null, null), new ReturnSelfToHandCost()),
                "{2}{U}, Discard a card: Return this creature to its owner's hand."
        ));
    }
}
