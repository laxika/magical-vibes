package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * A 1/1 creature; only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "222")
public class Slitherhead extends Card {

    public Slitherhead() {
        // Scavenge {0}
        addScavenge("{0}");
    }
}
