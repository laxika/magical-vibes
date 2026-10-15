package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "36")
public class UnwaveringInitiate extends Card {

    public UnwaveringInitiate() {
        // Vigilance is an auto-loaded keyword; no engine wiring needed here.

        // Embalm {4}{W} ({4}{W}, Exile this card from your graveyard: Create a token that's a copy of it,
        // except it's a white Zombie Human Warrior with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{4}{W}", "Human Warrior");
    }
}
