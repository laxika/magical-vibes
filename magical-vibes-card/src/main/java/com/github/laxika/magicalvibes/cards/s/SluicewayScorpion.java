package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * Deathtouch is auto-loaded from Scryfall; only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "198")
public class SluicewayScorpion extends Card {

    public SluicewayScorpion() {
        // Scavenge {1}{B}{G}
        addScavenge("{1}{B}{G}");
    }
}
