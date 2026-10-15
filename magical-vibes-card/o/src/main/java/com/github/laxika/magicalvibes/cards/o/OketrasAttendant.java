package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "22")
@CardRegistration(set = "AKR", collectorNumber = "28")
public class OketrasAttendant extends Card {

    public OketrasAttendant() {
        // Flying — intrinsic keyword, auto-loaded from Scryfall.

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");

        // Embalm {3}{W}{W} ({3}{W}{W}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Bird Soldier with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{3}{W}{W}", "Bird Soldier");
    }
}
