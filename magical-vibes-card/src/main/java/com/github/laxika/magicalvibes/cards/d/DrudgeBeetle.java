package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * A vanilla 2/2 body; only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "122")
public class DrudgeBeetle extends Card {

    public DrudgeBeetle() {
        // Scavenge {5}{G}
        addScavenge("{5}{G}");
    }
}
