package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOS", collectorNumber = "184")
public class DinasGuidance extends Card {

    public DinasGuidance() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardTypePredicate(CardType.CREATURE), LibrarySearchDestination.HAND_OR_GRAVEYARD));
    }
}
