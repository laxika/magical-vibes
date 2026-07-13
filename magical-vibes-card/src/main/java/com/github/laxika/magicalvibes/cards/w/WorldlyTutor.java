package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "6ED", collectorNumber = "269")
public class WorldlyTutor extends Card {

    public WorldlyTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardTypePredicate(CardType.CREATURE),
                LibrarySearchDestination.TOP_OF_LIBRARY));
    }
}
