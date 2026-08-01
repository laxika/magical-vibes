package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * A 3/3 with trample (a Scryfall-loaded keyword); only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "129")
public class KorozdaMonitor extends Card {

    public KorozdaMonitor() {
        // Scavenge {5}{G}{G}
        addScavenge("{5}{G}{G}");
    }
}
