package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "33")
public class PeaceOfMind extends Card {

    public PeaceOfMind() {
        // {W}, Discard a card: You gain 3 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new DiscardCardTypeCost(null, null), new GainLifeEffect(3)),
                "{W}, Discard a card: You gain 3 life."
        ));
    }
}
