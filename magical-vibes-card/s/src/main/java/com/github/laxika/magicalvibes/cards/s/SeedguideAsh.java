package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "235")
public class SeedguideAsh extends Card {

    public SeedguideAsh() {
        // When Seedguide Ash dies, you may search your library for up to three
        // Forest cards, put them onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new SearchLibraryEffect(
                        new Fixed(3),
                        new CardSubtypePredicate(CardSubtype.FOREST),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                "You may search your library for up to three Forest cards, put them onto the battlefield tapped, then shuffle."
        ));
    }
}
