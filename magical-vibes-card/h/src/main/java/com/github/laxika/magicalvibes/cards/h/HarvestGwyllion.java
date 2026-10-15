package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "EVE", collectorNumber = "90")
public class HarvestGwyllion extends Card {

    public HarvestGwyllion() {
        // Wither is auto-loaded from Scryfall keywords; the engine deals its
        // combat damage to creatures as -1/-1 counters. No extra logic needed.
    }
}
