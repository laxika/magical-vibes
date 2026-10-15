package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

/**
 * Road // Ruin — front half (Road).
 * Instant — Search your library for a basic land card, put it onto the battlefield tapped, then
 * shuffle.
 * Back half (Ruin) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "MH2", collectorNumber = "212")
public class RoadRuin extends Card {

    public RoadRuin() {
        setBackFaceCard(new Ruin());

        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }

    @Override
    public String getBackFaceClassName() {
        return "Ruin";
    }
}
