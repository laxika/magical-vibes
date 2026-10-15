package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "ARB", collectorNumber = "129")
public class Wargate extends Card {

    public Wargate() {
        // Search your library for a permanent card with mana value X or less,
        // put it onto the battlefield, then shuffle. (SearchLibraryEffect handles
        // the "then shuffle"; the spell goes to the graveyard normally.)
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardIsPermanentPredicate(),
                LibrarySearchDestination.BATTLEFIELD, new ManaValueBound(false, 0)));
    }
}
