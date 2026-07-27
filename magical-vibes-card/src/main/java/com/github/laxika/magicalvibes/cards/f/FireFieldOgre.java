package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "ALA", collectorNumber = "168")
public class FireFieldOgre extends Card {

    public FireFieldOgre() {
        // Unearth {U}{B}{R}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{U}{B}{R}");
    }
}
