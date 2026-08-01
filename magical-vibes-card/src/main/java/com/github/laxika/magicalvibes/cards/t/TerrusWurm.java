package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * A vanilla 5/5 body; only scavenge needs engine logic.
 */
@CardRegistration(set = "RTR", collectorNumber = "80")
public class TerrusWurm extends Card {

    public TerrusWurm() {
        // Scavenge {6}{B}
        addScavenge("{6}{B}");
    }
}
