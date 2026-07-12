package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "8ED", collectorNumber = "289")
@CardRegistration(set = "9ED", collectorNumber = "283")
@CardRegistration(set = "POR", collectorNumber = "195")
public class WoodElves extends Card {

    public WoodElves() {
        // When this creature enters, search your library for a Forest card,
        // put that card onto the battlefield, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryEffect(
                new CardSubtypePredicate(CardSubtype.FOREST), LibrarySearchDestination.BATTLEFIELD));
    }
}
