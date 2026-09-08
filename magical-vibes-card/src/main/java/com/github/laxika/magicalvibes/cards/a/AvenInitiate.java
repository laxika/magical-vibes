package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "43")
@CardRegistration(set = "AKR", collectorNumber = "50")
public class AvenInitiate extends Card {

    public AvenInitiate() {
        // Flying is an auto-loaded keyword; no engine wiring needed here.

        // Embalm {6}{U} ({6}{U}, Exile this card from your graveyard: Create a token that's a copy of it,
        // except it's a white Zombie Bird Warrior with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{6}{U}", "Bird Warrior");
    }
}
