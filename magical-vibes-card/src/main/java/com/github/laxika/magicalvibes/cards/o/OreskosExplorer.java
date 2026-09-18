package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreLandsThanController;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "C15", collectorNumber = "6")
public class OreskosExplorer extends Card {

    public OreskosExplorer() {
        // When this creature enters, search your library for up to X Plains cards, where X is the
        // number of players who control more lands than you. Reveal those cards, put them into your
        // hand, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(new OpponentsWithMoreLandsThanController(),
                        new CardSubtypePredicate(CardSubtype.PLAINS), LibrarySearchDestination.HAND));
    }
}
