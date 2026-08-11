package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WTH", collectorNumber = "63")
@CardRegistration(set = "ODY", collectorNumber = "118")
public class BuriedAlive extends Card {

    public BuriedAlive() {
        // Search your library for up to three creature cards, put them into your graveyard, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(3),
                new CardTypePredicate(CardType.CREATURE),
                LibrarySearchDestination.GRAVEYARD));
    }
}
