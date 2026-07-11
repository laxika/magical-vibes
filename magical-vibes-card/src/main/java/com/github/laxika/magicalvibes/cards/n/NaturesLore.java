package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "POR", collectorNumber = "178")
@CardRegistration(set = "P02", collectorNumber = "135")
public class NaturesLore extends Card {

    public NaturesLore() {
        // Search your library for a Forest card, put that card onto the
        // battlefield, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardSubtypePredicate(CardSubtype.FOREST), LibrarySearchDestination.BATTLEFIELD));
    }
}
