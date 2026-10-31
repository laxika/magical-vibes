package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandCardsToHandRestToBottomEffect;

@CardRegistration(set = "LRW", collectorNumber = "65")
public class FathomTrawl extends Card {

    public FathomTrawl() {
        addSpellEffect(new RevealUntilNonlandCardsToHandRestToBottomEffect(3));
    }
}
