package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToTopEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RAV", collectorNumber = "198")
public class CongregationAtDawn extends Card {

    public CongregationAtDawn() {
        addEffect(EffectSlot.SPELL,
                new SearchLibraryForCardsToTopEffect(new CardTypePredicate(CardType.CREATURE), 3));
    }
}
