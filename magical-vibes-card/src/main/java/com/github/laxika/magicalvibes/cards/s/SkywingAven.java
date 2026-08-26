package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "47")
public class SkywingAven extends Card {

    public SkywingAven() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new DiscardCardTypeCost(null, null), ReturnToHandEffect.self()),
                "Discard a card: Return this creature to its owner's hand."));
    }
}
