package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "M13", collectorNumber = "87")
public class DiabolicRevelation extends Card {

    public DiabolicRevelation() {
        // Search your library for up to X cards, put those cards into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new XValue(),
                null,
                LibrarySearchDestination.HAND));
    }
}
