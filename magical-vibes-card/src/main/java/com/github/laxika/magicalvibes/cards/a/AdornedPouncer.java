package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "HOU", collectorNumber = "2")
public class AdornedPouncer extends Card {

    public AdornedPouncer() {
        // Double strike is an auto-loaded keyword; no engine wiring needed here.

        // Eternalize {3}{W}{W} ({3}{W}{W}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a 4/4 black Zombie Cat with no mana cost. Eternalize only as a sorcery.)
        addEternalize("{3}{W}{W}", "Cat");
    }
}
