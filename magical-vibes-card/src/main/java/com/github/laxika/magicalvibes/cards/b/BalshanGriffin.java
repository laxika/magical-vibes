package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "67")
public class BalshanGriffin extends Card {

    public BalshanGriffin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new DiscardCardTypeCost(null, null), ReturnToHandEffect.self()),
                "{1}{U}, Discard a card: Return this creature to its owner's hand."
        ));
    }
}
