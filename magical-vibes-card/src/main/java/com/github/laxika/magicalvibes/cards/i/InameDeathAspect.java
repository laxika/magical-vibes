package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "118")
public class InameDeathAspect extends Card {

    public InameDeathAspect() {
        // When Iname enters, you may search your library for any number of Spirit cards, put them
        // into your graveyard, then shuffle. "Any number" is a library-sized up-to count; the
        // restricted search may fail to find, so the controller can stop after any number of picks.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardsInLibrary(CountScope.CONTROLLER),
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        LibrarySearchDestination.GRAVEYARD),
                "Search your library for any number of Spirit cards and put them into your graveyard?"
        ));
    }
}
