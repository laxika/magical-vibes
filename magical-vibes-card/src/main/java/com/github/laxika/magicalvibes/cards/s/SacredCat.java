package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "27")
public class SacredCat extends Card {

    public SacredCat() {
        // Lifelink is an auto-loaded keyword; no engine wiring needed here.

        // Embalm {W} ({W}, Exile this card from your graveyard: Create a token that's a copy of it,
        // except it's a white Zombie Cat with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{W}", "Cat");
    }
}
