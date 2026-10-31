package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndReturnMilledCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class ZoologicalStudy extends Card {

    public ZoologicalStudy() {
        addSpellEffect(new MillControllerAndReturnMilledCardToHandEffect(
                5,
                new CardTypePredicate(CardType.CREATURE)));
    }
}
