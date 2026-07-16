package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DRB", collectorNumber = "5")
public class Dragonstorm extends Card {

    public Dragonstorm() {
        // Search your library for a Dragon permanent card, put it onto the battlefield, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(new CardIsPermanentPredicate(), new CardSubtypePredicate(CardSubtype.DRAGON))),
                LibrarySearchDestination.BATTLEFIELD));

        // Storm (When you cast this spell, copy it for each spell cast before it this turn.)
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
