package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawUpToNGainLifePerCardBelowEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesHandAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;

@CardRegistration(set = "6ED", collectorNumber = "65")
public class DiminishingReturns extends Card {

    public DiminishingReturns() {
        // Each player shuffles their hand and graveyard into their library.
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesHandAndGraveyardIntoLibraryEffect());

        // You exile the top ten cards of your library.
        for (int i = 0; i < 10; i++) {
            addEffect(EffectSlot.SPELL, new ExileTopCardOfOwnLibraryEffect(false));
        }

        // Then each player draws up to seven cards (a per-player choice; no life gain).
        addEffect(EffectSlot.SPELL, new EachPlayerMayDrawUpToNGainLifePerCardBelowEffect(7, 0));
    }
}
