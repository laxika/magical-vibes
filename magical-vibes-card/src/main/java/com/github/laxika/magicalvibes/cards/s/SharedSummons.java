package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M20", collectorNumber = "193")
public class SharedSummons extends Card {

    public SharedSummons() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(2),
                new CardTypePredicate(CardType.CREATURE),
                LibrarySearchDestination.HAND,
                null,
                true));
    }
}
