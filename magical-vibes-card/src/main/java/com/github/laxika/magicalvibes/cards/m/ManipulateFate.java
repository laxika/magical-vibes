package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "INV", collectorNumber = "60")
public class ManipulateFate extends Card {

    public ManipulateFate() {
        // "Search your library for three cards, exile them, then shuffle."
        addEffect(EffectSlot.SPELL,
                new SearchLibraryEffect(new Fixed(3), null, LibrarySearchDestination.EXILE));
        // "Draw a card."
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
