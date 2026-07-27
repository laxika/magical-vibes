package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "HOU", collectorNumber = "42")
public class ProvenCombatant extends Card {

    public ProvenCombatant() {
        // Eternalize {4}{U}{U} ({4}{U}{U}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a 4/4 black Zombie Human Warrior with no mana cost. Eternalize only as a sorcery.)
        addEternalize("{4}{U}{U}", "Human Warrior");
    }
}
