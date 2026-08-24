package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WAR", collectorNumber = "18")
public class IgniteTheBeacon extends Card {

    public IgniteTheBeacon() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(2),
                new CardTypePredicate(CardType.PLANESWALKER),
                LibrarySearchDestination.HAND
        ));
    }
}
