package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * Haste is auto-loaded from Scryfall; only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "158")
public class DregMangler extends Card {

    public DregMangler() {
        // Scavenge {3}{B}{G}
        addScavenge("{3}{B}{G}");
    }
}
