package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "SHM", collectorNumber = "105")
public class RustrazorButcher extends Card {

    public RustrazorButcher() {
        // First strike and Wither are auto-loaded from Scryfall keywords; the
        // engine deals its first-strike combat damage to creatures as -1/-1
        // counters. No extra logic needed.
    }
}
