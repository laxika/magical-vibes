package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MOR", collectorNumber = "12")
public class IdyllicTutor extends Card {

    public IdyllicTutor() {
        // Search your library for an enchantment card, reveal it, put it into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardTypePredicate(CardType.ENCHANTMENT)));
    }
}
