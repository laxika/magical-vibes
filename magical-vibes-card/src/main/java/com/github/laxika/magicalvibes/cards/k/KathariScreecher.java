package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "ALA", collectorNumber = "47")
public class KathariScreecher extends Card {

    public KathariScreecher() {
        // Flying is an auto-loaded keyword; only Unearth needs engine logic.

        // Unearth {2}{U}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{2}{U}");
    }
}
