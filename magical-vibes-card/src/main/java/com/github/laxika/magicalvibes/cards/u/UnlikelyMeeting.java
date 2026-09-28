package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

public class UnlikelyMeeting extends Card {

    public UnlikelyMeeting() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardSubtypePredicate(CardSubtype.DOCTOR), LibrarySearchDestination.HAND));
    }
}
