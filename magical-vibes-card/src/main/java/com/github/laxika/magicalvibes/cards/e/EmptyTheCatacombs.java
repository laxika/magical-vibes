package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RAV", collectorNumber = "86")
public class EmptyTheCatacombs extends Card {

    public EmptyTheCatacombs() {
        addEffect(EffectSlot.SPELL, new EachPlayerReturnsCardsFromGraveyardToHandEffect(
                Integer.MAX_VALUE, new CardTypePredicate(CardType.CREATURE)));
    }
}
