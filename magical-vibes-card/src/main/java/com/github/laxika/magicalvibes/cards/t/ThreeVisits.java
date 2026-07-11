package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "PTK", collectorNumber = "153")
public class ThreeVisits extends Card {

    public ThreeVisits() {
        // Search your library for a Forest card, put it onto the battlefield,
        // then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardSubtypePredicate(CardSubtype.FOREST), LibrarySearchDestination.BATTLEFIELD));
    }
}
