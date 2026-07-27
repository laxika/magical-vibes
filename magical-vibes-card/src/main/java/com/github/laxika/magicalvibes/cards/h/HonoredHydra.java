package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "172")
public class HonoredHydra extends Card {

    public HonoredHydra() {
        // Trample is an auto-loaded keyword; no engine wiring needed here.

        // Embalm {3}{G} ({3}{G}, Exile this card from your graveyard: Create a token that's a copy of it,
        // except it's a white Zombie Snake Hydra with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{3}{G}", "Snake Hydra");
    }
}
