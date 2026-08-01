package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * A vanilla 5/5 body; only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "120")
public class DeadbridgeGoliath extends Card {

    public DeadbridgeGoliath() {
        // Scavenge {4}{G}{G}
        addScavenge("{4}{G}{G}");
    }
}
