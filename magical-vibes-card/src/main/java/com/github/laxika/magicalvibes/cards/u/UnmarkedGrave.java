package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

@CardRegistration(set = "MH2", collectorNumber = "106")
public class UnmarkedGrave extends Card {

    public UnmarkedGrave() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY)),
                LibrarySearchDestination.GRAVEYARD));
    }
}
