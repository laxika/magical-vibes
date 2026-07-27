package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;


@CardRegistration(set = "HOU", collectorNumber = "74")
public class RazakethsRite extends Card {

    public RazakethsRite() {
        // Search your library for a card, put that card into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());

        // Cycling {B} ({B}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{B}");
    }
}
