package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "ALA", collectorNumber = "74")
@CardRegistration(set = "HOP", collectorNumber = "26")
@CardRegistration(set = "DDN", collectorNumber = "5")
public class DregscapeZombie extends Card {

    public DregscapeZombie() {
        // Unearth {B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{B}");
    }
}
