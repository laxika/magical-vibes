package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TMP", collectorNumber = "73")
public class ManaSeverance extends Card {

    public ManaSeverance() {
        // "Search your library for any number of land cards, exile them, then shuffle."
        // "Any number" is a restricted (fail-to-find) search bounded by the library size, so the
        // controller may stop after any number of lands, including none.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardsInLibrary(CountScope.CONTROLLER),
                new CardTypePredicate(CardType.LAND),
                LibrarySearchDestination.EXILE));
    }
}
