package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "MSC", collectorNumber = "180")
public class Terramorph extends Card {

    public Terramorph() {
        // Search your library for a basic land card, put it onto the battlefield, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD));
    }
}
