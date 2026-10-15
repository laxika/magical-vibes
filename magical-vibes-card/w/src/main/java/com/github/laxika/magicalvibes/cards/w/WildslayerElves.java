package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "SHM", collectorNumber = "133")
public class WildslayerElves extends Card {

    public WildslayerElves() {
        // Wither is auto-loaded from Scryfall keywords; the engine deals its
        // combat damage to creatures as -1/-1 counters. No extra logic needed.
    }
}
