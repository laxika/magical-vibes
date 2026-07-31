package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.Mind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

/**
 * Spring // Mind — front half (Spring).
 * Sorcery — Search your library for a basic land card, put it onto the battlefield tapped, then shuffle.
 * Back half (Mind) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "219")
public class SpringMind extends Card {

    public SpringMind() {
        setBackFaceCard(new Mind());

        // Search your library for a basic land card, put it onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.SPELL,
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }

    @Override
    public String getBackFaceClassName() {
        return "Mind";
    }
}
