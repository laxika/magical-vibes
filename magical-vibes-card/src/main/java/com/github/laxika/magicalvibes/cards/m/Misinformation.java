package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;

import java.util.List;

/**
 * Misinformation — {B} Instant.
 * "Put up to three target cards from an opponent's graveyard on top of their library in any order."
 * The targets are chosen at cast time from a single opponent's graveyard; the order the cards are
 * chosen in is the order they are stacked (last chosen ends up on top).
 */
@CardRegistration(set = "ALL", collectorNumber = "56")
public class Misinformation extends Card {

    public Misinformation() {
        addEffect(EffectSlot.SPELL, PutTargetCardsFromGraveyardOnTopOfLibraryEffect
                .fromOpponentGraveyard(new CardAllOfPredicate(List.of()), 3));
    }
}
