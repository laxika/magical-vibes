package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "72")
public class TahCropSkirmisher extends Card {

    public TahCropSkirmisher() {
        // Embalm {3}{U} ({3}{U}, Exile this card from your graveyard: Create a token that's a copy of it,
        // except it's a white Zombie Snake Warrior with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{3}{U}", "Snake Warrior");
    }
}
