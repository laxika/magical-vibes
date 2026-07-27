package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "HOU", collectorNumber = "24")
public class SteadfastSentinel extends Card {

    public SteadfastSentinel() {
        // Vigilance is an auto-loaded keyword; no engine wiring needed here.

        // Eternalize {4}{W}{W} ({4}{W}{W}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a 4/4 black Zombie Human Cleric with no mana cost. Eternalize only as a sorcery.)
        addEternalize("{4}{W}{W}", "Human Cleric");
    }
}
