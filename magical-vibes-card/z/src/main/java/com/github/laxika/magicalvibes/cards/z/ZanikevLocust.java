package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * A 3/3 flier; flying comes from Scryfall keywords, only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "84")
public class ZanikevLocust extends Card {

    public ZanikevLocust() {
        // Scavenge {2}{B}{B}
        addScavenge("{2}{B}{B}");
    }
}
