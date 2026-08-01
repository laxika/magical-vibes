package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * Swampwalk is auto-loaded from Scryfall; only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "75")
public class SewerShambler extends Card {

    public SewerShambler() {
        // Scavenge {2}{B}
        addScavenge("{2}{B}");
    }
}
