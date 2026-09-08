package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BNG", collectorNumber = "24")
public class PleaForGuidance extends Card {

    public PleaForGuidance() {
        // Search your library for up to two enchantment cards, reveal them, put them into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(2),
                new CardTypePredicate(CardType.ENCHANTMENT), LibrarySearchDestination.HAND));
    }
}
