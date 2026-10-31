package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerKeepsCardsShufflesRestIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesUnspentManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "SHM", collectorNumber = "156")
public class Worldpurge extends Card {

    public Worldpurge() {
        // Return all permanents to their owners' hands.
        addSpellEffect(ReturnToHandEffect.allPermanentsMatching(null));
        // Each player chooses up to seven cards in their hand, then shuffles the rest into their library.
        addSpellEffect(new EachPlayerKeepsCardsShufflesRestIntoLibraryEffect(7));
        // Each player loses all unspent mana.
        addSpellEffect(new EachPlayerLosesUnspentManaEffect());
    }
}
