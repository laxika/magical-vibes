package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HOU", collectorNumber = "137")
public class UncageTheMenagerie extends Card {

    public UncageTheMenagerie() {
        // Search your library for up to X creature cards with different names that each have
        // mana value X, reveal them, put them into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new XValue(),
                new CardTypePredicate(CardType.CREATURE),
                LibrarySearchDestination.HAND,
                new ManaValueBound(true, 0),
                true));
    }
}
