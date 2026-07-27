package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "ARB", collectorNumber = "20")
public class EtheriumAbomination extends Card {

    public EtheriumAbomination() {
        // Unearth {1}{U}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step or if it would leave the battlefield.
        // Unearth only as a sorcery.
        addUnearth("{1}{U}{B}");
    }
}
