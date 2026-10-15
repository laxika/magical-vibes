package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * Reach is auto-loaded from Scryfall; only scavenge needs engine logic.
 */
@CardRegistration(set = "DGM", collectorNumber = "50")
public class ThrashingMossdog extends Card {

    public ThrashingMossdog() {
        // Scavenge {4}{G}{G}
        addScavenge("{4}{G}{G}");
    }
}
