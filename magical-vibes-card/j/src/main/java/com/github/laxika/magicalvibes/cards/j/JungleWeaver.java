package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "ALA", collectorNumber = "134")
public class JungleWeaver extends Card {

    public JungleWeaver() {
        // Reach is an intrinsic keyword (auto-loaded from Scryfall).
        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
