package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndReturnMilledCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class ZoologicalStudy extends Card {

    public ZoologicalStudy() {
        addEffect(EffectSlot.SPELL, new MillControllerAndReturnMilledCardToHandEffect(
                5,
                new CardTypePredicate(CardType.CREATURE)));
    }
}
