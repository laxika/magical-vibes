package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "VIS", collectorNumber = "96")
@CardRegistration(set = "TSB", collectorNumber = "69")
@CardRegistration(set = "DMR", collectorNumber = "145")
public class SuqAtaLancer extends Card {

    public SuqAtaLancer() {
        // Haste and flanking are auto-loaded from Scryfall and handled by the engine.
    }
}
