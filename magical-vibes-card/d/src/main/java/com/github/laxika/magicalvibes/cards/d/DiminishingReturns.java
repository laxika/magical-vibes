package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawUpToNGainLifePerCardBelowEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;

@CardRegistration(set = "6ED", collectorNumber = "65")
@CardRegistration(set = "ALL", collectorNumber = "26")
@CardRegistration(set = "ME1", collectorNumber = "32")
@CardRegistration(set = "EMA", collectorNumber = "46")
public class DiminishingReturns extends Card {

    public DiminishingReturns() {
        // Each player shuffles their hand and graveyard into their library.
        addSpellEffect(new EachPlayerShufflesZonesIntoLibraryEffect());

        // You exile the top ten cards of your library.
        for (int i = 0; i < 10; i++) {
            addSpellEffect(new ExileTopCardOfOwnLibraryEffect(false));
        }

        // Then each player draws up to seven cards: each chooses and draws before the next player chooses.
        addSpellEffect(EachPlayerMayDrawUpToNGainLifePerCardBelowEffect.drawUpToInTurnOrder(7));
    }
}
